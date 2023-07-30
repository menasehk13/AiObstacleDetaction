void sendCameraPicture(AsyncWebServerRequest *request) {
  camera_fb_t *fb = esp_camera_fb_get();
  if (!fb) {
    Serial.println("Frame buffer could not be acquired");
    request->send(500, "text/plain", "Frame buffer not available");
    return;
  }

  AsyncWebServerResponse *response = request->beginChunkedResponse("image/jpeg", [fb](uint8_t *buffer, size_t maxLen, size_t index, size_t total) -> size_t {
    if (index == 0) {
      return snprintf_P((char*)buffer, maxLen, "%s", "--frame\r\nContent-Type: image/jpeg\r\n\r\n");
    } else {
      size_t bytesRead = total - index;
      if (bytesRead > maxLen) {
        bytesRead = maxLen;
      }
      memcpy(buffer, fb->buf + index, bytesRead);
      return bytesRead;
    }
  });

  response->addHeader("Cache-Control", "no-cache");
  response->addHeader("Connection", "close");

  request->onDisconnect([fb]() {
    esp_camera_fb_return(fb);
  });

  request->send(response);
}


void handleRoot(AsyncWebServerRequest *request) {
  request->send(200, "text/html", "<img src='/video_feed' width='640' height='480' />");
}


  server.on("/video_feed", HTTP_GET, [](AsyncWebServerRequest *request){
    if (request->hasArg("stream") && request->arg("stream") == "mjpeg") {
      sendCameraPicture(request);
    } else {
      request->send(404);
    }
  });

  server.onNotFound([](AsyncWebServerRequest *request) {
    request->send(404);
  });
