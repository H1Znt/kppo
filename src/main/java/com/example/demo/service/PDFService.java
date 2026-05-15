package com.example.demo.service;

import com.example.demo.model.Alert;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.layout.element.Image;
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
      PdfFont font = PdfFontFactory.createFont(
        "C:/Windows/Fonts/arial.ttf",  
        PdfEncodings.IDENTITY_H        // режим кодировки: IDENTITY_H = Unicode (все языки)
      );

      // Заголовок
      Paragraph title = new Paragraph("ОТЧЕТ О ЗАКРЫТОМ ИНЦИДЕНТЕ")
              .setFont(font)         
              .setFontSize(18)
              .setTextAlignment(TextAlignment.CENTER)
              .setMarginBottom(20);
      document.add(title);
      
      // Информация об инциденте
      document.add(new Paragraph("ID инцидента: " + alert.getId()).setFont(font));
      document.add(new Paragraph("Тип события: " + alert.getType().name()).setFont(font));
      document.add(new Paragraph("Время события: " + alert.getTimestamp().format(DATE_FORMATTER)).setFont(font));
      document.add(new Paragraph("Статус: " + alert.getStatus().name()).setFont(font));
      
      if (alert.getDescription() != null && !alert.getDescription().isEmpty()) {
          document.add(new Paragraph("Описание: " + alert.getDescription()).setFont(font));
      }
      
      // Информация о датчике
      document.add(new Paragraph("\nИнформация о датчике:").setFont(font));
      document.add(new Paragraph("Модель: " + alert.getSensor().getModel()).setFont(font));
      document.add(new Paragraph("Местоположение: " + alert.getSensor().getLocation()).setFont(font));
      
      if (alert.getSensor().getAssignedTo() != null) {
          document.add(new Paragraph("Ответственный: " + alert.getSensor().getAssignedTo().getUsername()).setFont(font));
      }
      
      // Фотографии
      if (alert.getPhotoUrls() != null && !alert.getPhotoUrls().isEmpty()) {
          document.add(new Paragraph("\nПрикрепленные фотографии:").setFont(font));

          for (int i = 0; i < alert.getPhotoUrls().size(); i++) {
              String photoUrl = alert.getPhotoUrls().get(i);
              String relativePath = photoUrl.startsWith("/") ? photoUrl.substring(1) : photoUrl;
              Path imagePath = Paths.get(relativePath).toAbsolutePath();

              document.add(new Paragraph("Фото " + (i + 1) + ":").setFont(font));

              if (Files.exists(imagePath)) {
                  Image image = new Image(ImageDataFactory.create(imagePath.toString()));
                  image.setMaxWidth(400);
                  image.setMarginBottom(10);
                  document.add(image);
              } else {
                  document.add(new Paragraph("  (файл не найден: " + photoUrl + ")").setFont(font));
              }
          }
      }
      
      // Время закрытия
      document.add(new Paragraph("\nВремя закрытия инцидента: " + 
              java.time.LocalDateTime.now().format(DATE_FORMATTER)).setFont(font));
      
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
