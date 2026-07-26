package com.exam.common.constant;

/**
 * 系统常量
 */
public class Constants {

    /** 平台级 orgId */
    public static final Long PLATFORM_ORG_ID = 0L;

    /** 顶级父节点 ID */
    public static final Long ROOT_PARENT_ID = 0L;

    /** 默认密码 */
    public static final String DEFAULT_PASSWORD = "123456";

    /** 逻辑删除标记 */
    public static final int NOT_DELETED = 0;
    public static final int DELETED = 1;

    /** JWT Token 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** Redis Key 前缀 */
    public static final String REDIS_PREFIX = "exam:";
    public static final String REDIS_TOKEN_BLACKLIST = REDIS_PREFIX + "token:blacklist:";
    public static final String REDIS_EXAM_SESSION = REDIS_PREFIX + "exam:session:";
    public static final String REDIS_EXAM_ANSWERS = REDIS_PREFIX + "exam:answers:";
    public static final String REDIS_RATE_LIMIT = REDIS_PREFIX + "rate_limit:";
    public static final String REDIS_CAPTCHA = REDIS_PREFIX + "captcha:";
    public static final String REDIS_CONFIG = REDIS_PREFIX + "config:";

    /** 文件上传限制 */
    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    public static final long MAX_VIDEO_SIZE = 500 * 1024 * 1024; // 500MB

    /** 考试自动保存间隔（秒） */
    public static final int EXAM_AUTO_SAVE_INTERVAL = 30;

    /** 默认切屏限制次数 */
    public static final int DEFAULT_SCREEN_SWITCH_LIMIT = 3;

    private Constants() {}
}
