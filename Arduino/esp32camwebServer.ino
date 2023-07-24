#include "esp_camera.h"
#include "WiFi.h"
#include "WebSocketsServer.h"

const char* ssid = "test";
const char* password = "12615776";
const int webServerPort = 80;
WiFiServer server(webServerPort);
const int webSocketPort = 81;
WebSocketsServer webSocket = WebSocketsServer(webSocketPort);

void onWebSocketEvent(uint8_t num, WStype_t type, uint8_t* payload, size_t length) {
  if (type == WStype_TEXT) {
    String message = String((char*)payload);
    if (message == "f") {
      Serial.println("Forward");
    } else if (message == "l") {
      Serial.println("Left");
    } else if (message == "r") {
     Serial.println("Right");
    } else if (message == "d") {
      Serial.println("Down");
    } else {
     Serial.println("speed controller");
    }
  }
}

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }

  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = 5;
  config.pin_d1 = 18;
  config.pin_d2 = 19;
  config.pin_d3 = 21;
  config.pin_d4 = 36;
  config.pin_d5 = 39;
  config.pin_d6 = 34;
  config.pin_d7 = 35;
  config.pin_xclk = 0;
  config.pin_pclk = 22;
  config.pin_vsync = 25;
  config.pin_href = 23;
  config.pin_sscb_sda = 26;
  config.pin_sscb_scl = 27;
  config.pin_pwdn = 32;
  config.pin_reset = -1;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG;
  config.frame_size = FRAMESIZE_SVGA;
  config.jpeg_quality = 10;
  config.fb_count = 2;


  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.println("Camera initialization failed!");
    return;
  }

  Serial.println("Camera initialized!");

  Serial.print("ESP32 local IP: ");
  Serial.println(WiFi.localIP());

  server.begin();
  webSocket.begin();
  webSocket.onEvent(onWebSocketEvent);
}

void loop() {
WiFiClient client = server.available();
  if (client) {
    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: multipart/x-mixed-replace; boundary=frame");
    client.println();

    while (true) {
      camera_fb_t *fb = esp_camera_fb_get();
      if (!fb) {
        Serial.println("Camera capture failed");
        break;
      }
      client.print("--frame\r\n");
      client.print("Content-Type: image/jpeg\r\n");
      client.print("Content-Length: " + String(fb->len) + "\r\n");
      client.println("Cache-Control: no-store");
      client.print("\r\n");
      client.write(fb->buf, fb->len);
      client.print("\r\n");
      esp_camera_fb_return(fb);
    }
  }
  webSocket.loop();
  delay(1000);
}
