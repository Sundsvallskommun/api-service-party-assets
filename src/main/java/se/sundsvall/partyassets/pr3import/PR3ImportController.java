package se.sundsvall.partyassets.pr3import;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import se.sundsvall.partyassets.pr3import.PR3ImportMessagingClient.EmailRequest;
import se.sundsvall.partyassets.pr3import.PR3ImportMessagingClient.EmailRequest.Attachment;

@Controller
@RequestMapping("/import")
@ConditionalOnProperty(name = "pr3import.enabled", havingValue = "true", matchIfMissing = true)
class PR3ImportController {

    static final String CONTENT_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final PR3Importer importer;
    private final PR3ImportMessagingClient messagingClient;

    PR3ImportController(final PR3Importer importer, final PR3ImportMessagingClient messagingClient) {
        this.importer = importer;
        this.messagingClient = messagingClient;
    }

    @GetMapping(produces = TEXT_HTML_VALUE)
    String showForm() {
        return "/index";
    }

    @PostMapping(produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @ResponseBody
    PR3Importer.Result handleImport(@RequestParam("file") final MultipartFile file,
            @RequestParam("email") final String emailAddress) throws Exception {
        var result = importer.importFromExcel(file.getInputStream());

try (var out = new FileOutputStream("/tmp/apa.xlsx")) {
    IOUtils.copy(new ByteArrayInputStream(result.getFailedExcelData()), out);
}

        var message = String.format("Totalt %d post(er) varav %d lyckad(e) och %d misslyckade", result.getTotal(), result.getSuccessful(), result.getFailed());
        var emailRequest = new EmailRequest(
            emailAddress,
            "PR3 Import - misslyckade poster",
            message,
            List.of(new Attachment(
                "FAILED-" + file.getOriginalFilename(),
                CONTENT_TYPE_EXCEL,
                Base64.getEncoder().encodeToString(result.getFailedExcelData()))));
        messagingClient.sendEmail(emailRequest);

        return result;
    }
}
