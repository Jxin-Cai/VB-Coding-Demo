package com.example.planningpoker.domain.model.valueobject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 故事点数值对象
 * 
 * <p>表示估点的点数值，确保只能使用有效的斐波那契数列值。
 * 支持斐波那契数（1, 2, 3, 5, 8, 13），业务上建议使用不超过13的值。
 * 
 * <p>设计原则：
 * <ul>
 *   <li>不可变性：所有字段为final，无setter方法</li>
 *   <li>自我验证：使用数学公式动态验证斐波那契数</li>
 *   <li>静态工厂方法：使用of()方法创建实例</li>
 *   <li>常量池优化：常用值缓存在ConcurrentHashMap中</li>
 * </ul>
 * 
 * <p>数学原理：
 * 一个正整数n是斐波那契数，当且仅当 5*n²+4 或 5*n²-4 是完全平方数。
 * 
 * <p>Effective Java应用：
 * <ul>
 *   <li>Item 1: 使用静态工厂方法而非构造器</li>
 *   <li>Item 17: 最小化可变性</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-19 (更新：业务限制只到13)
 */
public final class StoryPoint {
    
    /**
     * 点数值（必须是正斐波那契数）
     */
    private final Integer value;
    
    /**
     * 常用的斐波那契数列值（用于常量池预初始化）
     * 业务上建议使用不超过13的值
     */
    private static final Set<Integer> COMMON_FIBONACCI = Set.of(
        1, 2, 3, 5, 8, 13
    );
    
    /**
     * 常量池 - 缓存已创建的实例，提升性能和内存效率
     * 使用 ConcurrentHashMap 确保线程安全
     */
    private static final Map<Integer, StoryPoint> CONSTANT_POOL = 
        new ConcurrentHashMap<>();
    
    // 静态初始化块 - 预填充常用值到常量池
    static {
        COMMON_FIBONACCI.forEach(v -> CONSTANT_POOL.put(v, new StoryPoint(v)));
    }
    
    /**
     * 预定义常量 - 1点（向后兼容）
     */
    public static final StoryPoint ONE = CONSTANT_POOL.get(1);
    
    /**
     * 预定义常量 - 2点（向后兼容）
     */
    public static final StoryPoint TWO = CONSTANT_POOL.get(2);
    
    /**
     * 预定义常量 - 3点（向后兼容）
     */
    public static final StoryPoint THREE = CONSTANT_POOL.get(3);
    
    /**
     * 预定义常量 - 5点（向后兼容）
     */
    public static final StoryPoint FIVE = CONSTANT_POOL.get(5);
    
    /**
     * 预定义常量 - 8点（向后兼容）
     */
    public static final StoryPoint EIGHT = CONSTANT_POOL.get(8);
    
    /**
     * 预定义常量 - 13点（新增）
     */
    public static final StoryPoint THIRTEEN = CONSTANT_POOL.get(13);
    
    /**
     * 私有构造函数，确保通过静态工厂方法创建
     * 
     * @param value 点数值
     * @throws IllegalArgumentException 如果点数值无效
     */
    private StoryPoint(Integer value) {
        validate(value);
        this.value = value;
    }
    
    /**
     * 静态工厂方法 - 创建故事点数对象
     * 
     * <p>使用数学公式动态验证斐波那契数，支持任意正斐波那契数。
     * 
     * <p>Effective Java Item 1: 静态工厂方法的优势：
     * <ul>
     *   <li>有名字：of()比new StoryPoint()更清晰</li>
     *   <li>可以返回缓存实例：使用常量池避免重复创建</li>
     *   <li>灵活验证：使用数学公式而非硬编码列表</li>
     * </ul>
     * 
     * @param value 点数值（必须是正斐波那契数）
     * @return 故事点数对象
     * @throws IllegalArgumentException 如果点数值为空或不是斐波那契数
     */
    public static StoryPoint of(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("故事点数不能为空");
        }
        
        // 验证是否为斐波那契数
        if (!isFibonacci(value)) {
            throw new IllegalArgumentException(
                "故事点数必须是斐波那契数列中的正整数，当前值：" + value
            );
        }
        
        // 从常量池获取或创建新实例（线程安全）
        return CONSTANT_POOL.computeIfAbsent(value, StoryPoint::new);
    }
    
    /**
     * 判断一个数是否为斐波那契数
     * 
     * <p>数学原理：一个正整数n是斐波那契数，当且仅当
     * 5*n² + 4 或 5*n² - 4 中至少有一个是完全平方数。
     * 
     * <p>时间复杂度：O(1) - 只需要简单的数学运算
     * 
     * @param n 要判断的数
     * @return 如果是斐波那契数返回true，否则返回false
     */
    private static boolean isFibonacci(int n) {
        if (n <= 0) {
            return false;
        }
        
        // 应用数学公式
        long n2 = (long) n * n;
        long expr1 = 5 * n2 + 4;
        long expr2 = 5 * n2 - 4;
        
        return isPerfectSquare(expr1) || isPerfectSquare(expr2);
    }
    
    /**
     * 判断一个数是否为完全平方数
     * 
     * <p>使用整数平方根验证：如果 sqrt(n)² = n，则n是完全平方数。
     * 
     * @param n 要判断的数
     * @return 如果是完全平方数返回true，否则返回false
     */
    private static boolean isPerfectSquare(long n) {
        if (n < 0) {
            return false;
        }
        long sqrt = (long) Math.sqrt(n);
        return sqrt * sqrt == n;
    }
    
    /**
     * 获取建议的斐波那契数列表（用于前端显示）
     * 
     * <p>生成不超过指定最大值的斐波那契数列。
     * 
     * @param maxValue 最大值（业务建议不超过13）
     * @return 斐波那契数列表
     */
    public static List<Integer> getSuggestedFibonacci(int maxValue) {
        List<Integer> result = new ArrayList<>();
        int a = 1, b = 1;
        
        while (a <= maxValue) {
            if (!result.contains(a)) {
                result.add(a);
            }
            int temp = a + b;
            a = b;
            b = temp;
        }
        
        return Collections.unmodifiableList(result);
    }
    
    /**
     * 验证规则（构造函数使用）
     * 
     * @param value 点数值
     * @throws IllegalArgumentException 如果点数值无效
     */
    private void validate(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("故事点数不能为空");
        }
        if (!isFibonacci(value)) {
            throw new IllegalArgumentException(
                "故事点数必须是斐波那契数列中的正整数，当前值：" + value
            );
        }
    }
    
    /**
     * 获取点数值
     * 
     * @return 点数值
     */
    public Integer getValue() {
        return value;
    }
    
    /**
     * 相等性比较（基于值）
     * 
     * <p>Effective Java Item 10: 覆盖equals方法时遵守通用约定
     * 
     * @param o 比较对象
     * @return 如果值相等返回true
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoryPoint that = (StoryPoint) o;
        return Objects.equals(value, that.value);
    }
    
    /**
     * 哈希码（基于值）
     * 
     * <p>Effective Java Item 11: 覆盖equals时总要覆盖hashCode
     * 
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    /**
     * 字符串表示
     * 
     * @return 格式化的字符串
     */
    @Override
    public String toString() {
        return "StoryPoint{" + value + "点}";
    }
}
