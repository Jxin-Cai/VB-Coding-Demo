package com.example.planningpoker.domain.exception;

/**
 * 故事卡未找到异常
 *
 * <p>当根据ID查询故事卡但不存在时抛出此异常。
 *
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class StoryCardNotFoundException extends RuntimeException {

    private final Long storyCardId;

    /**
     * 构造函数
     *
     * @param storyCardId 故事卡ID
     */
    public StoryCardNotFoundException(Long storyCardId) {
        super("故事卡不存在，ID: " + storyCardId);
        this.storyCardId = storyCardId;
    }

    /**
     * 构造函数（带自定义消息）
     *
     * @param storyCardId 故事卡ID
     * @param message 自定义错误消息
     */
    public StoryCardNotFoundException(Long storyCardId, String message) {
        super(message);
        this.storyCardId = storyCardId;
    }

    /**
     * 获取故事卡ID
     *
     * @return 故事卡ID
     */
    public Long getStoryCardId() {
        return storyCardId;
    }
}
