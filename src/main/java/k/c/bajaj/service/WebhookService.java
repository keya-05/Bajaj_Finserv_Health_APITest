package k.c.bajaj.service;

import k.c.bajaj.model.WebhookRequest;
import k.c.bajaj.model.WebhookResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String GENERATE_WEBHOOK_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    // ✅ FINAL SQL QUERY — Question 1 (odd regNo)
    private static final String FINAL_SQL_QUERY =
            "SELECT p.AMOUNT AS SALARY, " +
                    "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                    "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
                    "d.DEPARTMENT_NAME " +
                    "FROM PAYMENTS p " +
                    "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                    "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                    "WHERE DAY(p.PAYMENT_TIME) != 1 " +
                    "ORDER BY p.AMOUNT DESC " +
                    "LIMIT 1";

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            System.out.println("=== Step 1: Generating Webhook ===");

            WebhookRequest request = new WebhookRequest(
                    "Keya Chaudhary",
                    "ADT23SOCB0517",
                    "keyabhamu@gmail.com"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WebhookRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(
                    GENERATE_WEBHOOK_URL,
                    entity,
                    WebhookResponse.class
            );

            WebhookResponse webhookResponse = response.getBody();

            if (webhookResponse == null) {
                System.err.println("ERROR: Null response from webhook generation!");
                return;
            }

            String webhookUrl = webhookResponse.getWebhook();
            String accessToken = webhookResponse.getAccessToken();

            System.out.println("Webhook URL: " + webhookUrl);
            System.out.println("Access Token: " + accessToken);

            System.out.println("\n=== Step 2: Submitting SQL Query ===");
            submitSolution(webhookUrl, accessToken);

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void submitSolution(String webhookUrl, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", accessToken);

            Map<String, String> body = new HashMap<>();
            body.put("finalQuery", FINAL_SQL_QUERY);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    webhookUrl,
                    entity,
                    String.class
            );

            System.out.println("Submission Status: " + response.getStatusCode());
            System.out.println("Submission Response: " + response.getBody());

        } catch (Exception e) {
            System.err.println("Submission failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}