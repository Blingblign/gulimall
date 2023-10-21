package com.zzclearning.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.zzclearning.gulimall.search.config.GulimallESconfig;
import com.zzclearning.gulimall.search.constant.EsConstant;
import com.zzclearning.gulimall.search.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallSearchApplicationTests {
    @Autowired
    private RestHighLevelClient client;
    @Test
    void contextLoads() {
        System.out.println(client);
    }
    /**
     * 数据同步
     * 声明队列和交换机，binding key,新增和修改使用同一个队列，
     * indexRequest如果id不存在，新增，id存在全量替换,所以需要数据库中根据id重新查询数据然后写入es
     * exchange：                                hotel.topic
     * bindingKey:              hotel.insert                       hotel.delete
     * queue：                     hotel.insert.queue                      hotel.delete.queue
     * 酒店管理服务增删改发送消息给mq，routing key
     * 增：
     * 删：
     * 改：
     * 搜索服务监听队列，接收消息，更改es中信息
     */
    @Test
    public void dataSync() {
        
    }
    /**
     * 构建索引库映射
     *
     */
    @Test
    public void buildIndex() {
        System.out.println("EsConstant.MAPPING_TEMPLATE = " + EsConstant.MAPPING_TEMPLATE);
        System.out.println("EsConstant.SETTING_TEMPLATE = " + EsConstant.SETTING_TEMPLATE);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("my_sku_test");
        createIndexRequest.settings(EsConstant.SETTING_TEMPLATE,XContentType.JSON).mapping(EsConstant.MAPPING_TEMPLATE, XContentType.JSON);
        try {
            CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 构建自动补全请求
     * 查询附近酒店
     */
    @Test
    public void suggestRequest() {
        String suggestName = "text_suggestion";
        String keyWord = "O";
        SearchRequest searchRequest = new SearchRequest(EsConstant.PRODUCT_INDEX);
        CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("suggestion").size(10).skipDuplicates(true).text(keyWord);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion(suggestName,suggestion);
        searchRequest.source().suggest(suggestBuilder);

        try {
            SearchResponse suggestionResp = client.search(searchRequest, RequestOptions.DEFAULT);
            List<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestEntries =
                    suggestionResp.getSuggest().getSuggestion(suggestName).getEntries();
            List<Text> collect = suggestEntries.get(0).getOptions().stream().map(Suggest.Suggestion.Entry.Option::getText).collect(Collectors.toList());
            System.out.println(collect);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    /**
     * 构建检索请求
     * 查询附近酒店
     */
    @Test
    public void searchRequest() throws IOException {
        SearchRequest searchRequest = new SearchRequest("hotel");
        SearchSourceBuilder sourceBuilder = searchRequest.source();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //按关键词和 距离10km内
        boolQuery.must(QueryBuilders.matchQuery("name","外滩"))
                .filter(QueryBuilders.geoDistanceQuery("location").distance("10", DistanceUnit.KILOMETERS).point(40,60));
        //修改相关性评分
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                //原始查询
                boolQuery,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        //function score 元素
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                //过滤条件
                                QueryBuilders.termQuery("isAd",true),
                                //算分函数
                                ScoreFunctionBuilders.weightFactorFunction(10))})
                //加权模式
                .boostMode(CombineFunction.MULTIPLY);


        sourceBuilder.query(functionScoreQueryBuilder);
        sourceBuilder.aggregation(AggregationBuilders.terms("brandAgg").field("brand").size(20).subAggregation(AggregationBuilders.avg("priceAgg").field("price")).subAggregation(AggregationBuilders.max("maxPriceAgg").field("price")));
        sourceBuilder.sort(SortBuilders.geoDistanceSort("location",40,60).order(SortOrder.ASC).unit(DistanceUnit.KILOMETERS));
        sourceBuilder.from(0).size(10);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Arrays.stream(searchResponse.getHits().getHits()).forEach((hit)-> {
            String sourceAsString = hit.getSourceAsString();
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            Text fragment = highlightFields.get("").getFragments()[0];
        });
        System.out.println(searchResponse.getHits().getHits().length);
    }
    /**
     * 查看索引库
     */
    @Test
    public void getIndex() throws IOException {
        GetIndexRequest newusers = new GetIndexRequest("newusers");
        boolean exists = client.indices().exists(newusers, RequestOptions.DEFAULT);
        if (exists) {
            GetIndexResponse getIndexResponse = client.indices().get(newusers, RequestOptions.DEFAULT);
            System.out.println(getIndexResponse.getMappings().get("newusers").getSourceAsMap());
        } else System.out.println("索引不存在");

    }

    /**
     * 查看一条文档
     */
    @Test
    public void getDoc() throws IOException {
        GetRequest userReq = new GetRequest("newusers", "1");
        GetResponse documentFields = client.get(userReq, RequestOptions.DEFAULT);
        String sourceAsString = documentFields.getSourceAsString();
        User user = JSON.parseObject(sourceAsString, User.class);
        System.out.println(user);
        System.out.println(sourceAsString);
    }

    /**
     * 修改文档
     */
    @Test
    public void updateDoc() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("newusers", "1");
        UpdateRequest doc = updateRequest.doc("name", "李思思2", "age", 18);
        UpdateResponse update = client.update(doc, RequestOptions.DEFAULT);
        System.out.println(update.status());
    }

    /**
     * 新增/替换文档
     * @throws IOException
     */
    @Test
    void indexData() throws IOException {
        // 1、构建创建或更新请求，指定索引users
        IndexRequest indexRequest = new IndexRequest("newusers");
        // 2、设置id
        indexRequest.id("1");// 数据的id
        // 方式一：直接设置数据项
        //users.source("userName", "zhangsan", "gender", "M", "age", "18");
        User user = new User("lisi", "M", 22, 10006l);
        // 3、绑定数据与请求
        // 方式二：设置json串格式数据
        indexRequest.source(JSON.toJSONString(user), XContentType.JSON);
        // 4、执行：同步
        IndexResponse index = client.index(indexRequest, GulimallESconfig.COMMON_OPTIONS);

        // 5、提取响应数据
        System.out.println(index);
    }


    /**
     * 从es中查询数据
     * 1、创建检索请求
     *      SearchRequest searchRequest = new SearchRequest("newbank")
     * 2、创建检索条件构建对象（SearchSourceBuilder用于构建检索条件的builder对象）
     *      SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
     *      绑定操作：
     *         sourceBuilder.sort("age");
     *         sourceBuilder.from(1);
     *         sourceBuilder.size(10);
     *         sourceBuilder.aggregation(AggregationBuilders.terms("ageAgg").field("age").size(10));
     *         sourceBuilder.aggregation(AggregationBuilders.avg("balanceAvg").field("balance"));
     *         sourceBuilder.query(QueryBuilders.matchAllQuery("address", "mill"));
     *         sourceBuilder.query(QueryBuilders.boolQuery());
     * 3、请求绑定条件
     *      searchRequest.source(sourceBuilder);
     * 4、执行请求，接收结果
     * SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
     * 5、获取命中结果
     *      SearchHit[] hits = searchResponse.getHits().getHits();
     *      Account account = JSON.parseObject(hit.getSourceAsString(), Account.class);
     * 6、获取分组结果
     *      Aggregations aggregations = searchResponse.getAggregations();
     *      年龄分布
     *      Terms ageAgg = aggregations.get("ageAgg");
     *      for (Terms.Bucket bucket : ageAgg.getBuckets()) {
     *          System.out.println("年龄：" + bucket.getKeyAsString() + "--人数： " + bucket.getDocCount());
     *      }
     *      平均工资
     *      Avg balanceAvg = aggregations.get("balanceAvg");
     *      double balance = balanceAvg.getValue();
     */



}
