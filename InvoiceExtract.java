public class InvoiceExtract {
  public JSONObject pdfReader(String context, String feature, String objectentity, Users user) {
        OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

        String apiKey = OPENAI_KEY;
        String extractedText = context;
        String json_format = "{ "
            + "\"invoice_number\": \"\","
            + "\"invoice_date\": \"MMM dd, yyyy\","
            + "\"due_date\": \"MMM dd, yyyy\","
            + "\"serial_number\": \"\","
            + "\"status\": \"\","
            + "\"tax_agency_code\": \"\","
            + "\"buyer_info\": {"
            + "  \"company_name\": \"\","
            + "  \"address\": \"\","
            + "  \"phone_number\": \"\","
            + "  \"email\": \"\","
            + "  \"tax_code\": \"\""
            + "},"
            + "\"seller_info\": {"
            + "  \"company_name\": \"\","
            + "  \"address\": \"\","
            + "  \"phone_number\": \"\","
            + "  \"email\": \"\","
            + "  \"tax_code\": \"\""
            + "},"
            + "\"items\": ["
            + "  {"
            + "    \"item_description\": \"\","
            + "    \"time_duration\": \"\""
            + "    \"quantity\": ,"
            + "    \"unit_price\": ,"
            + "    \"total_price\": "
            + "  }"
            + "],"
            + "\"sub_total_value\": ,"
            + "\"total_value\": ,"
            + "\"tax\": ,"
            + "\"discount_value:\": ,"
            + "\"shipping_fee\": ,"
            + "\"payment_method\": ,"
            + "\"policy\": \"\","
            + "\"notes\": \"\","
            + "\"currency_unit\": \"\","
            + "\"summary\": \"\""
            + "}";
        String prompt = "When you receive an invoice, it contains crucial information regarding a transaction. Your task is to convert the summarized text invoice into a structured JSON format with the lists of identify key information.\n" +
                        "\n" +
                        "Here are the identify key information and their descriptions that will be included in our JSON structure:\n" +
                        "\n" +
                        "invoice_number: A unique identifier for this invoice, used for tracking and reference.\n" +
                        "invoice_date: The date when the invoice was issued.\n" +
                        "serial_number: Often used for additional tracking or reference.\n" +
                        "status: Invoice is Paid or Unpaid.\n" +
                        "tax_agency_code: Identifies the tax agency if applicable.\n" +
                        "buyer_info: A nested object containing information about the buyer, including:\n" +
                        "company_name: The name of the company or individual purchasing the goods or services.\n" +
                        "address: The physical address of the buyer.\n" +
                        "phone_number: The contact telephone number for the buyer.\n" +
                        "email: The contact email address for the buyer.\n" +
                        "tax_code: A code for tax purposes, possibly related to VAT or other tax identification.\n" +
                        "seller_info: A nested object containing information about the seller, similar in structure to buyer_info.\n" +
                        "items: An array of objects, each representing an item or service being billed, including:\n" +
                        "item_description: A more detailed description of the item or service.\n" +
                        "time_duration: Intended for services billed by time.\n" +
                        "quantity: The number of units of the item being purchased.\n" +
                        "unit_price: The price per unit of the item.\n" +
                        "total_price: The total price for the item (quantity x unit price). Only allow positive number.\n" +
                        "VAT_rate: The rate of Value Added Tax applicable to the item. Only allow positive number.\n" +
                        "tax_amount: The amount of tax for the item. Only allow positive number.\n" +
                        "sub_total_value: The total before adding taxes and shipping fees. Only allow positive number.\n" +
                        "total_value: The final total after all calculations. Only allow positive number.\n" +
                        "tax: The total amount of tax on the invoice. Only allow positive number.\n" +
                        "shipping_fee: Any additional Shipping/Freight/Handling fees. Only allow positive number.\n" +
                        "payment_method: How the payment was made, e.g., \"Credit\".\n" +
                        "policy: Any policy information related to the transaction.\n" +
                        "notes: Space for any additional notes about the invoice.\n" +
                        "currency_unit: The unit of currency for the transaction.\n" +
                        "summary: A summary or conclusion of the invoice.\n" +
                        "Here are the summarized text invoice that need to map with the identify key information JSON format:\n" +
                        "\n" + 
                        extractedText +
                        "%s\n" +
                        "\n" +
                        "Guidance:\n" +
                        "\n" +
                        "Accuracy is Crucial: Ensure the information from the invoice is accurately represented in the JSON document. Mistakes or inaccuracies can lead to misunderstandings or data processing errors.\n" +
                        "Follow the Structure: Adhere to the provided definitions and structure for organizing the invoice data. This ensures consistency and ease of understanding for anyone reviewing the JSON document.\n" +
                        "Handle Missing Data Appropriately: If certain information is not present on the invoice or is not applicable, handle it as instructed (leave the field empty).\n" +
                        "Use the Correct Data Types: Be mindful of data types when entering information (e.g., numbers should be unquoted, strings should be within quotes).\n" +
                        "Output Format:\n" +
                        "Please format the answer as a JSON structure with the format corresponding with below:\n" +
                        json_format+
                        "\n" +
                        "Return only the JSON and no other commentary. This is not a code snippet so don't add three backtick prefixes and suffixes to the response. Thanks.";
        
        JSONObject payload = new JSONObject()
                .put("model", "gpt-4o-2024-05-13") // Use the appropriate model version
                .put("messages", new JSONObject[]{new JSONObject().put("role", "user").put("content",
                        prompt)})
                .put("temperature", 0);
//                .put("stream", true);
        
//        String OPENAI_API_DOMAIN = PropertiesUtil.getProperty(
//			EnumProperty.OPENAI_API_DOMAIN);
//        String url = OPENAI_API_DOMAIN
//                        .replace("[VERSION]",
//                                        ConstantsCustom.OPEN_API_VERSION)
//                        .replace("[ENDPOINT]",
//                                        ConstantsCustom.CHAT_GPT_ENDPOINT);
        okhttp3.RequestBody body = okhttp3.RequestBody.create(payload.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();
        Date requesttime = new Date();
        try (Response response = client.newCall(request).execute()) {
            Date responsetime = new Date();
            JSONObject jsonResponse = new JSONObject(response.body().string());
            // Access the "choices" array
            JSONArray choices = jsonResponse.getJSONArray("choices");

            // Access the first object in the array (assuming there's at least one)
            JSONObject firstChoice = choices.getJSONObject(0);

            // Access the "message" object and then the "content" field
            String content = firstChoice.getJSONObject("message").getString("content");
            content = content.replaceAll("\n", "");
            content = content.replaceAll("```json\\s*|\\s*```", "");
            JSONObject resultContent = new JSONObject(content);
            Long token = jsonResponse.getJSONObject("usage").getLong("total_tokens");
            resultContent.put("token", token);
            
             Long totaltoken = jsonResponse.getJSONObject("usage").getLong("total_tokens");
            Long requesttokens = jsonResponse.getJSONObject("usage").getLong("prompt_tokens");
            Long responsetokens = jsonResponse.getJSONObject("usage").getLong("completion_tokens");
            
            srvChatGPTLogs.save(user, feature, 
                    requesttime, responsetime, 
                    prompt, 
                    requesttokens.doubleValue(), 
                    responsetokens.doubleValue(), 
                    totaltoken.doubleValue(), 
                    content, 
                    objectentity, 
                    null);
            return resultContent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String extractTextFromPDF(String pdfUrl) {
        HttpURLConnection connection = null;
        PDDocument document = null;
        try {
            URL url = new URL(pdfUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            // Get the localhost address
            InetAddress localhost = InetAddress.getLocalHost();
            // Set the Referer header using the local host name
            conn.setRequestProperty("Referer", "https://" + localhost.getHostName()
                    + "/");

            try (InputStream in = conn.getInputStream()) {
                document = PDDocument.load(in);
                PDFTextStripper pdfStripper = new PDFTextStripper();
                return pdfStripper.getText(document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
    
    public JSONObject imageReader(String path, String feature, String objectentity, Users user) throws IOException {
//        OkHttpClient client = new OkHttpClient();
        OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.MINUTES)
            .build();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String json_format = "{ "
            + "\"invoice_number\": \"\","
            + "\"invoice_date\": \"MMM dd, yyyy\","
            + "\"due_date\": \"MMM dd, yyyy\","
            + "\"serial_number\": \"\","
            + "\"status\": \"\","
            + "\"tax_agency_code\": \"\","
            + "\"buyer_info\": {"
            + "  \"company_name\": \"\","
            + "  \"address\": \"\","
            + "  \"phone_number\": \"\","
            + "  \"email\": \"\","
            + "  \"tax_code\": \"\""
            + "},"
            + "\"seller_info\": {"
            + "  \"company_name\": \"\","
            + "  \"address\": \"\","
            + "  \"phone_number\": \"\","
            + "  \"email\": \"\","
            + "  \"tax_code\": \"\""
            + "},"
            + "\"items\": ["
            + "  {"
            + "    \"item_description\": \"\","
            + "    \"time_duration\": \"\""
            + "    \"quantity\": ,"
            + "    \"unit_price\": ,"
            + "    \"total_price\": "
            + "  }"
            + "],"
            + "\"sub_total_value\": ,"
            + "\"total_value\": ,"
            + "\"tax\": ,"
            + "\"discount_value:\": ,"
            + "\"shipping_fee\": ,"
            + "\"payment_method\": ,"
            + "\"policy\": \"\","
            + "\"notes\": \"\","
            + "\"currency_unit\": \"\","
            + "\"summary\": \"\""
            + "}";
        
        String prompt = "When you receive IMAGES, it contains crucial information regarding a transaction. Your task is to convert the image invoice into a structured JSON format with the lists of identify key information.\n" +
                        "\n" +
                        "Here are the identify key information and their descriptions that will be included in our JSON structure:\n" +
                        "\n" +
                        "invoice_number: A unique identifier for this invoice, used for tracking and reference.\n" +
                        "invoice_date: The date when the invoice was issued.\n" +
                        "status: Invoice is Paid or Unpaid.\n" +
                        "serial_number: Often used for additional tracking or reference.\n" +
                        "tax_agency_code: Identifies the tax agency if applicable.\n" +
                        "buyer_info: A nested object containing information about the buyer, including:\n" +
                        "company_name: The name of the company or individual purchasing the goods or services.\n" +
                        "address: The physical address of the buyer.\n" +
                        "phone_number: The contact telephone number for the buyer.\n" +
                        "email: The contact email address for the buyer.\n" +
                        "tax_code: A code for tax purposes, possibly related to VAT or other tax identification.\n" +
                        "seller_info: A nested object containing information about the seller, similar in structure to buyer_info.\n" +
                        "items: An array of objects, each representing an item or service being billed, including:\n" +
                        "item_description: A more detailed description of the item or service.\n" +
                        "time_duration: Intended for services billed by time.\n" +
                        "quantity: The number of units of the item being purchased.\n" +
                        "unit_price: The price per unit of the item.\n" +
                        "total_price: The total price for the item (quantity x unit price). Only allow positive number.\n" +
                        "VAT_rate: The rate of Value Added Tax applicable to the item. Only allow positive number.\n" +
                        "tax_amount: The amount of tax for the item. Only allow positive number.\n" +
                        "sub_total_value: The total before adding taxes and shipping fees. Only allow positive number.\n" +
                        "total_value: The final total after all calculations. Only allow positive number.\n" +
                        "tax: The total amount of tax on the invoice. Only allow positive number.\n" +
                        "shipping_fee: Any additional Shipping/Freight/Handling fees. Only allow positive number.\n" +
                        "payment_method: How the payment was made, e.g., \"Credit\".\n" +
                        "policy: Any policy information related to the transaction.\n" +
                        "notes: Space for any additional notes about the invoice.\n" +
                        "currency_unit: The unit of currency for the transaction.\n" +
                        "summary: A summary or conclusion of the invoice.\n" +
                        "\n" +
                        "Guidance:\n" +
                        "\n" +
                        "Accuracy is Crucial: Ensure the information from the invoice is accurately represented in the JSON document. Mistakes or inaccuracies can lead to misunderstandings or data processing errors.\n" +
                        "Follow the Structure: Adhere to the provided definitions and structure for organizing the invoice data. This ensures consistency and ease of understanding for anyone reviewing the JSON document.\n" +
                        "Handle Missing Data Appropriately: If certain information is not present on the invoice or is not applicable, handle it as instructed (leave the field empty).\n" +
                        "Use the Correct Data Types: Be mindful of data types when entering information (e.g., numbers should be unquoted, strings should be within quotes).\n" +
                        "Output Format:\n" +
                        "Please format the answer as a JSON structure with the format corresponding with below:\n" +
                        json_format+
                        "\n" +
                        "Return only the JSON and no other commentary. This is not a code snippet so don't add three backtick prefixes and suffixes to the response. Thanks.";
        JSONObject messageContent1 = new JSONObject();
        messageContent1.put("type", "text");
        messageContent1.put("text", prompt);

        

        JSONArray messageContents = new JSONArray();
        messageContents.put(messageContent1);
        if (path.endsWith(".pdf")) {
            List<String> base64Images = convertPDFToBase64Images(path);
            if(null != base64Images) {
                for(String base64 : base64Images) {
                    JSONObject messageContent2 = new JSONObject();
                    messageContent2.put("type", "image_url");
                    messageContent2.put("image_url", new JSONObject().put("url", "data:image/jpeg;base64," + base64));
                    messageContents.put(messageContent2);
                }
            }
        } else {
            JSONObject messageContent2 = new JSONObject();
                    messageContent2.put("type", "image_url");
                    messageContent2.put("image_url", new JSONObject().put("url", path));
                    messageContents.put(messageContent2);
        }
        

        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", messageContents);

        JSONArray messages = new JSONArray();
        messages.put(message);

        JSONObject body = new JSONObject();
        body.put("model", "gpt-4o-2024-05-13");
        body.put("messages", messages);
//        body.put("max_tokens", 300);
        body.put("temperature", 0);
//        body.put("stream", true);

        okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(body.toString(), JSON);
        Request request = new Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer " + OPENAI_KEY)
            .post(requestBody)
            .build();

        
        Date requesttime = new Date();
        try (Response response = client.newCall(request).execute()) {
            Date responsetime = new Date();
            JSONObject jsonResponse = new JSONObject(response.body().string());
            // Access the "choices" array
            JSONArray choices = jsonResponse.getJSONArray("choices");

            // Access the first object in the array (assuming there's at least one)
            JSONObject firstChoice = choices.getJSONObject(0);

            // Access the "message" object and then the "content" field
            String content = firstChoice.getJSONObject("message").getString("content");
            content = content.replaceAll("\n", "");
            content = content.replaceAll("```json\\s*|\\s*```", "");
            JSONObject resultContent = new JSONObject(content);
            Long token = jsonResponse.getJSONObject("usage").getLong("total_tokens");
            resultContent.put("token", token);
            
            Long totaltoken = jsonResponse.getJSONObject("usage").getLong("total_tokens");
            Long requesttokens = jsonResponse.getJSONObject("usage").getLong("prompt_tokens");
            Long responsetokens = jsonResponse.getJSONObject("usage").getLong("completion_tokens");
            
            srvChatGPTLogs.save(user, feature, 
                    requesttime, responsetime, 
                    prompt, 
                    requesttokens.doubleValue(), 
                    responsetokens.doubleValue(), 
                    totaltoken.doubleValue(), 
                    content, 
                    objectentity, 
                    null);
            
            return resultContent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static List<String> convertPDFToBase64Images(String pdfUrl) {
        List<String> base64Images = new ArrayList<>();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(pdfUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            // Get the localhost address
            InetAddress localhost = InetAddress.getLocalHost();
            // Set the Referer header using the local host name
            conn.setRequestProperty("Referer", "https://" + localhost.getHostName()
                    + "/");

            InputStream in = conn.getInputStream();
            PDDocument document = PDDocument.load(in);
            PDFRenderer renderer = new PDFRenderer(document);

            for (int i = 0; i < document.getNumberOfPages(); ++i) {
                BufferedImage image = renderer.renderImageWithDPI(i, 300);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpeg", baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                base64Images.add(base64Image);
            }

            document.close();
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return base64Images;
    }
  
}
