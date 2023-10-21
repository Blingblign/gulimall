package com.zzclearning.gulimall.search.constant;

import java.io.*;

/**
 * @author bling
 * @create 2023-02-06 15:12
 */
public class EsConstant {
    public static final String  PRODUCT_INDEX    = "my_sku_test";//商品在es中的索引
    public static final Integer PRODUCT_PAGESIZE = 2;//商品每页展示数量
    public static String MAPPING_TEMPLATE = "{\n" + "    \"properties\": {\n" + "      \"skuId\": {\n" + "        \"type\": \"long\"\n" + "      },\n" + "      \"spuId\": {\n" + "        \"type\": \"keyword\"\n" + "      },\n" + "      \"skuTitle\": {\n" + "        \"type\": \"text\",\n" + "        \"analyzer\": \"text_analyzer\",\n" + "        \"search_analyzer\": \"ik_smart\",\n" + "        \"copy_to\": \"all\"\n" + "      },\n" + "      \"skuImage\": {\n" + "        \"type\": \"keyword\"\n" + "      },\n" + "      \"skuPrice\": {\n" + "        \"type\": \"double\"\n" + "      },\n" + "      \"saleCount\": {\n" + "        \"type\": \"long\"\n" + "\n" + "      },\n" + "      \"hasStock\": {\n" + "        \"type\": \"boolean\"\n" + "      },\n" + "      \"hotScore\": {\n" + "        \"type\": \"long\"\n" + "      },\n" + "      \"brandId\": {\n" + "        \"type\": \"long\"\n" + "\n" + "      },\n" + "      \"brandName\": {\n" + "        \"type\": \"keyword\",\n" + "        \"copy_to\": \"all\"\n" + "      },\n" + "      \"brandImage\": {\n" + "        \"type\": \"keyword\"\n" + "      },\n" + "      \"catalogId\": {\n" + "        \"type\": \"long\"\n" + "\n" + "      },\n" + "      \"catalogName\": {\n" + "        \"type\": \"keyword\",\n" + "        \"copy_to\": \"all\"\n" + "      },\n" + "      \"attrs\": {\n" + "        \"type\": \"nested\",\n" + "        \"properties\": {\n" + "          \"attrId\": {\n" + "            \"type\": \"long\"\n" + "          },\n" + "          \"attrName\": {\n" + "            \"type\": \"keyword\"\n" + "          },\n" + "          \"attrValue\": {\n" + "            \"type\": \"keyword\"\n" + "          }\n" + "        }\n" + "      },\n" + "      \"all\": {\n" + "        \"type\": \"text\",\n" + "        \"analyzer\": \"text_analyzer\",\n" + "        \"search_analyzer\": \"ik_smart\"\n" + "      },\n" + "      \"suggestion\": {\n" + "        \"type\": \"completion\",\n" + "        \"analyzer\": \"completion_analyzer\",\n" + "        \"search_analyzer\": \"ik_smart\"\n" + "      }\n" + "    }\n" + "  }";
    public static String SETTING_TEMPLATE = "{\n" + "    \"analysis\": {\n" + "      \"analyzer\": {\n" + "        \"text_analyzer\": {\n" + "          \"tokenizer\": \"ik_max_word\",\n" + "          \"filter\": \"pinyin_filter\"\n" + "        },\n" + "        \"completion_analyzer\": {\n" + "          \"tokenizer\": \"keyword\",\n" + "          \"filter\": \"pinyin_filter\"\n" + "        }\n" + "      },\n" + "      \"filter\": {\n" + "        \"pinyin_filter\": {\n" + "          \"type\": \"pinyin\",\n" + "          \"keep_full_pinyin\": false,\n" + "          \"keep_joined_full_pinyin\": true,\n" + "          \"keep_original\": true,\n" + "          \"limit_first_letter_length\": 16,\n" + "          \"remove_duplicated_term\": true,\n" + "          \"none_chinese_pinyin_tokenize\": false\n" + "        }\n" + "      }\n" + "    }\n" + "  }";

    //static {
    //
    //     try(BufferedReader mappingReader = new BufferedReader(new InputStreamReader(new FileInputStream("static/mapping.json")));
    //         BufferedReader settingReader = new BufferedReader(new InputStreamReader(new FileInputStream("static/setting.json")))) {
    //         MAPPING_TEMPLATE = readFromJson(mappingReader);
    //         SETTING_TEMPLATE = readFromJson(settingReader);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    //}
    private static String readFromJson(BufferedReader reader) throws IOException {
        String str = "";
        StringBuilder builder = new StringBuilder();
        while ((str = reader.readLine()) != null) {
            builder.append(str);
        }
        return builder.toString();
    }
}

