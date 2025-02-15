package esawy.giza.email_schduler.controller;


import esawy.giza.email_schduler.payload.EmailRequest;
import esawy.giza.email_schduler.payload.EmailResponse;
import esawy.giza.email_schduler.quartz.job.EmailJob;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;


@Slf4j
@RestController
public class EmailSchedulerController {

    final private Scheduler scheduler;
    final private JavaMailSender mailSender;

    public EmailSchedulerController(Scheduler scheduler, JavaMailSender mailSender) {
        this.scheduler = scheduler;
        this.mailSender = mailSender;
    }


    @PostMapping("/schedule/email")
    public ResponseEntity<EmailResponse> scheduleEmail(@Valid @RequestBody EmailRequest emailRequest) {
        try {
            ZonedDateTime dateTime = ZonedDateTime.of(emailRequest.getDateTime(), emailRequest.getTimeZone());
            if (dateTime.isBefore(ZonedDateTime.now())) {
                return ResponseEntity.badRequest().body(
                        new EmailResponse(false, "dateTime must be after current time"));
            }

            JobDetail jobDetail = buildJobDetail(emailRequest);
            Trigger trigger = buildTrigger(jobDetail, dateTime);
            scheduler.scheduleJob(jobDetail, trigger);

            return ResponseEntity.ok(
                    new EmailResponse(true, jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), "Email Scheduled Successfully"));
        } catch (SchedulerException ex) {
            log.error("Error scheduling email", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new EmailResponse(false, "Error scheduling email"));
        }
    }

    @GetMapping("/schedule/email")
    public ResponseEntity<String> getAllScheduledEmails() {

        return ResponseEntity.ok("Emails retrieved successfully");
    }

    private JobDetail buildJobDetail(EmailRequest schedulerEmailRequest) {
        JobDataMap jobDetailMap = new JobDataMap();

        jobDetailMap.put("to", schedulerEmailRequest.getTo());
        jobDetailMap.put("subject", schedulerEmailRequest.getSubject());
        jobDetailMap.put("body", schedulerEmailRequest.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                .withDescription("Send Email Job")
                .usingJobData(jobDetailMap)
                .storeDurably()
                .build();

    }

    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt) {

        return TriggerBuilder
                .newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Send Email Trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

    // Create endpoint to send a static email immediately
    @GetMapping("/send/email")
    public ResponseEntity<EmailResponse> sendEmail() {
        try {
            sendMail("oesawy610@gmail.com",
                    "omar.m.elesawy2002@gmail.com",
                    "Test Subject",
                    "Test Body");
            return ResponseEntity.ok(new EmailResponse(true, "Email Sent Successfully"));
        } catch (Exception ex) {
            log.error("Error sending email", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new EmailResponse(false, "Error sending email"));
        }
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
