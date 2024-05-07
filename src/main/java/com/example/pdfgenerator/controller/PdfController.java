package com.example.pdfgenerator.controller;

import com.example.pdfgenerator.model.Invoice;
import com.example.pdfgenerator.services.PdfService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RestController
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @PostMapping("/generatePDF")
    public ResponseEntity<String> generatePDF(@RequestBody Invoice invoice) {
        try {
            String hash = pdfService.generateHash(invoice);
            String fileName = hash + ".pdf";
            String filePath = "generated_pdfs/" + fileName;
            File pdfFile = new File(filePath);
            if (pdfFile.exists()) {
                return ResponseEntity.ok().body("PDF is already generated for the given data with path : "+pdfFile);
            }

            Document document = new Document();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, byteArrayOutputStream);

            document.open();
            pdfService.addContentToPDF(document, invoice);
            document.close();

            filePath = pdfService.savePDFToLocalStorage(byteArrayOutputStream.toByteArray(), fileName);

            return ResponseEntity.ok().body(filePath);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}