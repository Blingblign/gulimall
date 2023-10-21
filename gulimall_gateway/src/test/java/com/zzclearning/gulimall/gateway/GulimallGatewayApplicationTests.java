package com.zzclearning.gulimall.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GulimallGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
    @Test
    public void testSearch() {
        int[] nums = {5,7,7,8,8,10};
        int search = search(nums, 8);
        System.out.println(search);
    }

    public int search(int[] nums, int target) {
        //二分法解决排序查找数字问题
        //方法一：分别求得右边界：找到第一个比target大的数；求得左边界：找到第一个比target小的数
        //方法二：由于数组中的数字都是非递减的且都是整数，所以出现次数=target的右边界-（target-1）的有边界
        int i = 0;int j = nums.length - 1;
        //查找右边界 right:最后i左边都是比target小的数,j右边都是大于target的数,二分法逐渐缩小区间最后i = j,mid = i = j；-->i 比j大1；
        while (i <= j) {
            int mid = (i + j) >> 2;
            if (nums[mid] <= target) i = mid + 1;
            else j = mid - 1;
        }
        int right = i;//nums[i] 是第一个比target大的数
        //如果j < 0 ：数组中的数都比target大；如果 j >= 0 且 nums[j] != target，则不存在target；
        if (j < 0 || nums[j] != target) return 0;
        //查询左边界
        i = 0; j = right -1;
        while (i <= j) {
            int mid = (i + j) >> 2;
            if (nums[mid] < target) i = mid + 1;
            else j = mid - 1;
        }
        int left = j;
        return right - left - 1;

    }

}
