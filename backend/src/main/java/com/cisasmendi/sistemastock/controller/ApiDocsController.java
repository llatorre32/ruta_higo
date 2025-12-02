package com.cisasmendi.sistemastock.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ApiDocsController {

    @GetMapping("/api/docs")
    @ResponseBody
    public String scalarApiDocs() {
        return """
<!DOCTYPE html>
<html>
<head>
    <title>Sistema de Stock API</title>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<body>
    <script
        id="api-reference"
        data-url="/api/v3/api-docs"
        data-configuration='{
            "theme": "default",
            "hideDownloadButton": false,
            "hideTestRequestButton": false,
            "hideModels": false,
            "defaultOpenAllTags": false
        }'
    ></script>
    <script src="https://cdn.jsdelivr.net/npm/@scalar/api-reference"></script>
</body>
</html>
                """;
    }

    @GetMapping("/")
    public String redirectToApiDocs() {
        return "redirect:/api/docs";
    }
}