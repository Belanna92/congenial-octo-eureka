public class Geek {

    private final String name;
    private final long phoneNumber;
    private final String emailAddress;
    private final String message;


    public Geek(String name, long phoneNumber, String emailAddress, String message) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.message = message;
    }


    public String getName() {
        return name;
    }
    public long getPhoneNumber() {
        return phoneNumber;
    }
    public String getEmailAddress() {
        return emailAddress;
    }
    public String getMessage() {
        return message;
    }
}
