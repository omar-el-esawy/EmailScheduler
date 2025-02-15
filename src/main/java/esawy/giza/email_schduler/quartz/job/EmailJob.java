package esawy.giza.email_schduler.quartz.job;


import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class EmailJob extends QuartzJobBean {

    final private JavaMailSender mailSender;

    final private MailProperties mailProperties;


    public EmailJob(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

        String subject = jobDataMap.getString("subject");
        String to = jobDataMap.getString("to");
        String body = jobDataMap.getString("body");

        sendMail(mailProperties.getUsername(), to, subject, body);
    }

    private void sendMail(String from, String to, String subject, String body) {
        try {
            mailSender.send(mimeMessage -> {
                mimeMessage.setFrom(from);
                mimeMessage.setRecipients(jakarta.mail.Message.RecipientType.TO, to);
                mimeMessage.setSubject(subject);
                mimeMessage.setText(body);
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
