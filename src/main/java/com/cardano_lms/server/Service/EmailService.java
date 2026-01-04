package com.cardano_lms.server.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${app.mail.fromName:Cardano LMS}")
    private String fromName;

    @Value("${SENDGRID_API_KEY}")
    private String sendGridApiKey;

    @Value("${SEND_EMAIL_FROM}")
    private String sendEmailFrom;


    public void sendVerificationCode(String to, String code) {
        if (!mailEnabled) {
            return;
        }
        sendVerificationCodeViaSendGrid(to, code);
    }

    private void sendVerificationCodeViaSendGrid(String to, String code) {
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            return;
        }

        try {

            Email from = new Email(sendEmailFrom != null && !sendEmailFrom.isBlank() ? sendEmailFrom : "phandinhnghia2004ht@gmail.com");
            from.setName(fromName);
            Email recipient = new Email(to);

            String subject = "Mã xác thực tài khoản";

            String html = "" +
                    "<div style='font-family:Inter,system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;max-width:540px;margin:24px auto;padding:24px;border:1px solid #e5e7eb;border-radius:12px;background:#ffffff'>" +
                    "  <div style='display:flex;align-items:center;gap:8px;margin-bottom:16px'>" +
                    "    <div style='width:8px;height:8px;border-radius:9999px;background:#2563eb'></div>" +
                    "    <div style='font-weight:600;color:#111827'>Cardano LMS</div>" +
                    "  </div>" +
                    "  <h2 style='margin:0 0 12px 0;color:#111827;font-size:20px;font-weight:700'>Xác thực email của bạn</h2>" +
                    "  <p style='margin:0 0 16px 0;color:#374151;font-size:14px;line-height:1.6'>Cảm ơn bạn đã đăng ký. Vui lòng dùng mã bên dưới để xác thực email. Mã sẽ hết hạn sau 10 phút.</p>" +
                    "  <div style='margin:16px 0;padding:14px 16px;border-radius:10px;border:1px dashed #2563eb;background:#eff6ff;color:#1e3a8a;font-weight:700;font-size:18px;letter-spacing:2px;text-align:center'>" + code + "</div>" +
                    "  <div style='margin-top:16px;border-top:1px solid #e5e7eb;padding-top:12px;color:#6b7280;font-size:12px'>" +
                    "    Nếu bạn không yêu cầu đăng ký, hãy bỏ qua email này." +
                    "  </div>" +
                    "</div>";

            Content textContentObj = new Content("text/plain", "Mã xác thực của bạn là: " + code + "\nMã sẽ hết hạn sau 10 phút.");
            Content htmlContentObj = new Content("text/html", html);

            Mail mail = new Mail(from, subject, recipient, textContentObj);
            mail.addContent(htmlContentObj);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            }
        } catch (IOException ignored) {
        }
    }


}
