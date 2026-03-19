package com.example.demo.controller;

import com.example.demo.service.PhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PhotoUploadController {

  private static final Logger logger = LoggerFactory.getLogger(PhotoUploadController.class);

  @Autowired
  private PhotoService photoService;

  @GetMapping("/incidents/{id}/upload")
  public String showUploadForm(@PathVariable Long id, Model model) {
    logger.info("Showing upload form for incident ID: {}", id);
    model.addAttribute("alertId", id);
    return "upload-photos"; // Имя шаблона Thymeleaf
  }

  @PostMapping("/incidents/{id}/upload")
  public String handleFileUpload(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files, RedirectAttributes redirectAttributes) {
    logger.info("Handling file upload for incident ID: {}", id);

    try {
      List<String> photoUrls = photoService.uploadPhotos(id, files);
      redirectAttributes.addFlashAttribute("message", "Successfully uploaded " + photoUrls.size() + " photos");
      logger.info("Photos uploaded successfully for incident ID: {}", id);

    } catch (Exception e) {
      logger.error("Error uploading photos", e);
      redirectAttributes.addFlashAttribute("error", "Failed to upload photos: " + e.getMessage());
    }
    
    return "redirect:/incidents/" + id + "/upload";
  }
}
