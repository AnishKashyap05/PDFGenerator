package com.example.pdfgenerator.services;

import com.example.pdfgenerator.model.Invoice;
import com.example.pdfgenerator.model.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class PdfService {
    public String generateHash(Invoice invoice) throws NoSuchAlgorithmException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(invoice);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(jsonData.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte hashByte : hashBytes) {
            String hex = Integer.toHexString(0xff & hashByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.substring(0,8);
    }


    public void addContentToPDF(Document document, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        PdfPCell sellerCell = new PdfPCell();
        sellerCell.addElement(new Phrase("Seller: " + invoice.getSeller()));
        sellerCell.addElement(new Phrase("GSTIN: " + invoice.getSellerGstin()));
        sellerCell.addElement(new Phrase("Address: " + invoice.getSellerAddress()));
        sellerCell.setColspan(2);
        table.addCell(sellerCell);

        PdfPCell buyerCell = new PdfPCell();
        buyerCell.addElement(new Phrase("Buyer: " + invoice.getBuyer()));
        buyerCell.addElement(new Phrase("GSTIN: " + invoice.getBuyerGstin()));
        buyerCell.addElement(new Phrase("Address: " + invoice.getBuyerAddress()));
        buyerCell.setColspan(2);
        table.addCell(buyerCell);

        table.addCell("Item");
        table.addCell("Quantity");
        table.addCell("Rate");
        table.addCell("Amount");

        for (Item item : invoice.getItems()) {
            table.addCell(item.getName());
            table.addCell(item.getQuantity());
            table.addCell(String.valueOf(item.getRate()));
            table.addCell(String.valueOf(item.getAmount()));
        }

        document.add(table);
    }

    public String savePDFToLocalStorage(byte[] pdfBytes, String fileName) throws IOException {
        String storagePath = "generated_pdfs/";
        String filePath = storagePath + fileName;
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        fileOutputStream.write(pdfBytes);
        fileOutputStream.close();
        return filePath;
    }
}
