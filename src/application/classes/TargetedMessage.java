package application.classes;

public class TargetedMessage
{
    public static final int TARGET_USER = 0;
    public static final int TARGET_DISCUSS = 1;
    public static final int TARGET_GROUP = 2;

    public long sourceId;
    public int targetType;
    public long targetId;
    public String content;

    public TargetedMessage(final long p_sourceId, final int p_targetType, final long p_targetId, final String p_content)
    {
        sourceId = p_sourceId;
        targetType = p_targetType;
        targetId = p_targetId;
        content = p_content;
    }
}
