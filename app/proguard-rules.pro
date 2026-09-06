# TickTokBoost release rules (v0.0.9+)
#
# The app uses no reflection; JSON persistence is manual (org.json) and all
# Compose/resources are referenced directly, so R8 default optimize rules are
# sufficient. One belt-and-braces rule: enum constants are persisted by NAME
# in SharedPreferences (TxType/TxStatus/DisputeStatus/QuestStatus/...).

-keepclassmembers enum com.tiktokboost.app.data.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
