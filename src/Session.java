public class Session {
    private static int user_id;
    private static String account_no;

    public static void setUser_id(int id) {
        user_id = id;
    }

    public static int getUser_id() {
        return user_id;
    }

    public static void setAccount_no(String no) {
        account_no = no;
    }

    public static String getAccount_no() {
        return account_no;
    }
}