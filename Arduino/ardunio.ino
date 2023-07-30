  server.on("/", HTTP_GET, [](AsyncWebServerRequest *request) {
    request->send(200, "text/html", "<img src='/video_feed' width='640' height='480' />");
  });

  server.on("/video_feed", HTTP_GET, [](AsyncWebServerRequest *request) {
    request->onDisconnect([]() {
      Serial.println("Client disconnected");
      client.stop();
    });

    request->send_P(200, "multipart/x-mixed-replace; boundary=frame", nullptr, nullptr, [](uint8_t *buffer, size_t maxLen, size_t index) -> size_t {
      if (!client.connected()) {
        Serial.println("Client not connected");
        return 0;
      }

      if (index == 0) {
        camera_fb_t *fb = esp_camera_fb_get();
        if (!fb) {
          Serial.println("Camera capture failed");
          return 0;
        }
        size_t len = snprintf_P((char*)buffer, maxLen, "--frame\r\nContent-Type: image/jpeg\r\nContent-Length: %u\r\n\r\n", fb->len);
        memcpy(buffer + len, fb->buf, fb->len);
        esp_camera_fb_return(fb);
        return len + fb->len;
      }

      // Ensure no more data is sent when the client is disconnected
      if (!client.connected()) {
        Serial.println("Client disconnected during transmission");
        return 0;
      }

      // Add a small delay to simulate the framerate (30 FPS in this case)
      delay(33);

      return 0;
    });
  });
