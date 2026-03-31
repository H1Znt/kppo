package com.example.demo.service;

import com.example.demo.model.Alert;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
public class PDFService {

  private static final Logger logger = LoggerFactory.getLogger(PDFService.class);

  @Value("${app.upload.dir:uploads}")
  private String uploadDir;

  private static final DateTimeFormatter DATE_FORMATTER =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

  public String generateAlertReport (Alert alert) {
    logger.info("Generating PDF report for alert ID: {}", alert.getId());

    try {
      Path reportsDir = Paths.get(uploadDir, "reports");
      Files.createDirectories(reportsDir);

      String filename = "alert_" + alert.getId() + "_report.pdf";
      Path filePath = reportsDir.resolve(filename);

      // Пишем PDF сразу в файл
      PdfWriter writer = new PdfWriter(filePath.toFile());
      PdfDocument pdf = new PdfDocument(writer);
      Document document = new Document(pdf);
      
      // Заголовок
      Paragraph title = new Paragraph("ОТЧЕТ О ЗАКРЫТОМ ИНЦИДЕНТЕ")
              .setFontSize(18)
              .setTextAlignment(TextAlignment.CENTER)
              .setMarginBottom(20);
      document.add(title);
      
      // Информация об инциденте
      document.add(new Paragraph("ID инцидента: " + alert.getId()));
      document.add(new Paragraph("Тип события: " + alert.getType().name()));
      document.add(new Paragraph("Время события: " + alert.getTimestamp().format(DATE_FORMATTER)));
      document.add(new Paragraph("Статус: " + alert.getStatus().name()));
      
      if (alert.getDescription() != null && !alert.getDescription().isEmpty()) {
          document.add(new Paragraph("Описание: " + alert.getDescription()));
      }
      
      // Информация о датчике
      document.add(new Paragraph("\nИнформация о датчике:"));
      document.add(new Paragraph("Модель: " + alert.getSensor().getModel()));
      document.add(new Paragraph("Местоположение: " + alert.getSensor().getLocation()));
      
      if (alert.getSensor().getAssignedTo() != null) {
          document.add(new Paragraph("Ответственный: " + alert.getSensor().getAssignedTo().getUsername()));
      }
      
      // Фотографии
      if (alert.getPhotoUrls() != null && !alert.getPhotoUrls().isEmpty()) {
          document.add(new Paragraph("\nПрикрепленные фотографии:"));
          for (int i = 0; i < alert.getPhotoUrls().size(); i++) {
              document.add(new Paragraph("Фото " + (i + 1) + ": " + alert.getPhotoUrls().get(i)));
          }
      }
      
      // Время закрытия
      document.add(new Paragraph("\nВремя закрытия инцидента: " + 
              java.time.LocalDateTime.now().format(DATE_FORMATTER)));
      
      document.close();
      
      String reportUrl = "/uploads/reports/" + filename;
      logger.info("PDF report generated: {}", reportUrl);
      return reportUrl;
      
    } catch (IOException e) {
        logger.error("Error generating PDF report", e);
        throw new RuntimeException("Failed to generate PDF report", e);
    }
  }

  // Удаление PDF-отчёта с диска после удаления инцидента
  public void deleteReportPdf(Long alertId) {
    try {
      Path reportsDir = Paths.get(uploadDir, "reports").normalize();
      String filename = "alert_" + alertId + "_report.pdf";
      Path filePath = reportsDir.resolve(filename).normalize();
      if (!filePath.startsWith(reportsDir)) {
        logger.warn("Unsafe report path rejected: {}", filePath);
        return;
      }
      if (Files.deleteIfExists(filePath)) {
        logger.info("Deleted PDF report for alert ID: {}", alertId);
      }
    } catch (IOException e) {
      logger.error("Failed to delete PDF for alert ID: {}", alertId, e);
    }
  }
}
