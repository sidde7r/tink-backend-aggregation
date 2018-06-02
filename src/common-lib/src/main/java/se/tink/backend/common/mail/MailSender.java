package se.tink.backend.common.mail;

import com.google.api.client.util.Base64;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MergeVar;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MergeVarBucket;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.Recipient;
import com.microtripit.mandrillapp.lutung.view.MandrillMessageStatus;
import com.microtripit.mandrillapp.lutung.view.MandrillTemplate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import se.tink.backend.core.SubscriptionType;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;

public class MailSender {
    private final MandrillApi service;

    private static final LogUtils log = new LogUtils(MailSender.class);

    private LoadingCache<String, MandrillTemplate> templates;
    private final boolean isProduction;

    private final SubscriptionHelper subscriptionHelper;

    @Inject
    public MailSender(SubscriptionHelper subscriptionHelper,
            @Named("productionMode") boolean isProduction,
            MandrillApi mandrillApi) {
        this.subscriptionHelper = subscriptionHelper;
        this.isProduction = isProduction;
        this.service = mandrillApi;
    }

    @PostConstruct
    public void initialize() {
        this.templates = CacheBuilder.newBuilder().maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES).build(new CacheLoader<String, MandrillTemplate>() {
                    @Override
                    public MandrillTemplate load(String templateKey) throws Exception {
                        try {
                            return service.templates().info(templateKey);
                        } catch (MandrillApiError e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

    }

    /**
     * Returns true if the email was sent to Mandrill, false otherwise
     */
    public boolean sendMessage(String toAddress, String subject, String fromAddress, String fromName, String content) {
        return sendMessage(toAddress, subject, fromAddress, fromName, content, false);
    }

    public boolean sendMessage(String toAddress, String subject, String fromAddress, String fromName, String content,
            boolean isSensitive) {
        return sendMessage(toAddress, subject, fromAddress, fromName, content, isSensitive, null);
    }

    public boolean sendMessage(String toAddress, String subject, String fromAddress, String fromName, String content,
            boolean isSensitive, Map<String, byte[]> attachments) {
        if (Strings.isNullOrEmpty(toAddress)) {
            log.error("Empty recipient email address. Aborting.");
            return false;
        }

        try {
            MandrillMessage message = getMandrillMessage(toAddress, subject, fromAddress, fromName, content);

            if (attachments != null) {
                List<MandrillMessage.MessageContent> mandrillAttachments = Lists.newArrayList();
                attachments.entrySet().forEach(
                        entry ->
                        {
                            String name = entry.getKey();
                            byte[] pdf = entry.getValue();
                            MandrillMessage.MessageContent attachment = new MandrillMessage.MessageContent();
                            attachment.setName(name);
                            attachment.setContent(Base64.encodeBase64String(pdf));
                            attachment.setBinary(true);
                            mandrillAttachments.add(attachment);
                        }
                );
                message.setAttachments(mandrillAttachments);
            }

            message.setViewContentLink(!isSensitive);

            service.messages().send(message, true);
            return true;

        } catch (MandrillApiError e) {
            log.error("Error while communicating with Mandrill.", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Unknown error while trying to send email.", e);
            throw new RuntimeException(e);
        }
    }

    private MandrillMessage getMandrillMessage(String toAddress, String subject, String fromAddress, String fromName,
            String content) {
        MandrillMessage message = new MandrillMessage();

        message.setHtml(content);
        message.setAutoText(true);
        message.setFromEmail(fromAddress);
        message.setFromName(fromName);
        if (isProduction) {
            message.setSubject(subject);
        } else {
            message.setSubject(String.format("[STAGING] %s", subject));
        }
        MergeVarBucket bucket = new MergeVarBucket();
        bucket.setRcpt(toAddress);

        List<Recipient> recipients = Lists.newArrayList();
        Recipient recipient = new Recipient();
        recipient.setEmail(toAddress);
        recipients.add(recipient);

        message.setTo(recipients);
        return message;
    }

    private boolean subscribesTo(User user, SubscriptionType type) {
        return subscriptionHelper.subscribesTo(user.getId(), type);
    }

    /**
     * Returns true if the email was sent to Mandrill, false otherwise
     */
    public boolean sendMessageWithTemplate(User user, MailTemplate template, Map<String, String> parameters) {

        if (Strings.isNullOrEmpty(user.getUsername())) {
            return false;
        }

        // Verify user subscribes to this email type
        if (template.getSubscriptionType() != null && !subscribesTo(user, template.getSubscriptionType())) {
            return false;
        }

        String locale = user.getProfile().getLocale();

        MandrillMessage message = new MandrillMessage();

        try {
            MandrillTemplate messageTemplate = templates.get(template.getIdentifier() + "-" +
                    locale.toLowerCase().replace("_", "-"));

            message.setHtml(messageTemplate.getCode());
            message.setAutoText(true);
            message.setFromEmail(messageTemplate.getFromEmail());
            message.setFromName(messageTemplate.getFromName());
            if (isProduction) {
                message.setSubject(messageTemplate.getSubject());
            } else {
                message.setSubject(String.format("[STAGING] %s", messageTemplate.getSubject()));
            }

            MergeVarBucket bucket = new MergeVarBucket();
            bucket.setRcpt(user.getUsername());

            List<MergeVar> variables = Lists.newArrayList();

            if (parameters != null && parameters.size() > 0) {
                for (String key : parameters.keySet()) {
                    MergeVar variable = new MergeVar();
                    variable.setName(key);
                    variable.setContent(parameters.get(key));
                    variables.add(variable);
                }
            }

            MergeVar userIdVariable = new MergeVar();
            userIdVariable.setName("USER_ID");
            userIdVariable.setContent(user.getId());
            variables.add(userIdVariable);

            String token = subscriptionHelper.getOrCreateTokenFor(user.getId());

            MergeVar subscriptionTokenVariable = new MergeVar();
            subscriptionTokenVariable.setName("SUBSCRIPTION_TOKEN");
            subscriptionTokenVariable.setContent(token);
            variables.add(subscriptionTokenVariable);

            bucket.setVars(variables.toArray(new MergeVar[variables.size()]));

            message.setMergeVars(Lists.newArrayList(bucket));

            List<Recipient> recipients = Lists.newArrayList();
            Recipient recipient = new Recipient();
            recipient.setEmail(user.getUsername());
            recipients.add(recipient);

            message.setTo(recipients);

            if (template.isSensitive()) {
                message.setViewContentLink(false);
            }

            MandrillMessageStatus[] statuses = service.messages().send(message, true);

            if (statuses != null) {
                for (MandrillMessageStatus status : statuses) {

                    /**
                     * From Mandrill API web regarding async flag
                     * https://mandrillapp.com/api/docs/messages.JSON.html#method=send
                     *
                     * Enable a background sending mode that is optimized for bulk sending.
                     * In async mode, messages/send will immediately return a status of "queued" for every recipient.
                     * To handle rejections when sending in async mode, set up a webhook for the 'reject' event.
                     * Defaults to false for messages with no more than 10 recipients;
                     * messages with more than 10 recipients are always sent asynchronously,
                     * regardless of the value of async.
                     *
                     */
                    if (!"sent".equals(status.getStatus()) && !"queued".equals(status.getStatus())) {
                        log.warn(user.getId(), String.format(
                                "MailSender: Was not able to send email. { template: %s, reason: %s, status: %s }",
                                template.getIdentifier(), status.getRejectReason(), status.getStatus()));
                    }
                }
            }

            return true;
        } catch (MandrillApiError e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // There is no default SensitiveMessage value because callee should be forced to take a stance.
    public boolean sendMessageWithTemplate(User user, MailTemplate template) {
        return sendMessageWithTemplate(user, template, null);
    }
}
