package pl.ziwg.backend.notificator.email;

public interface IEmailSender {
    void sendMail(String mail, String message);
}
