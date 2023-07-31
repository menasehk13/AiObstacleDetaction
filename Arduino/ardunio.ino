#include "esp_camera.h"
#include <Arduino.h>
#include <AsyncTCP.h>
#include "ESPAsyncWebServer.h"

const char* WIFI_SSID = "ssid";
const char* WIFI_PASS = "password";
const int in1 = 12;   
const int in2 = 13;   
const int enA = 15;
const int in3 = 14;
const int in4 = 2;
const int enB= 15;
int leftSpeed = 200;   
int rightSpeed = 200; 

AsyncWebServer server(81);
AsyncWebSocket wsCarInput("/drive");

void onCarInputWebSocketEvent(AsyncWebSocket *server,
                              AsyncWebSocketClient *client,
                              AwsEventType type,
                              void *arg,
                              uint8_t *data,
                              size_t len)
                      
{
   switch (type)
  {
    case WS_EVT_CONNECT:
      Serial.printf("WebSocket client #%u connected from %s\n", client->id(), client->remoteIP().toString().c_str());
      break;

    case WS_EVT_DISCONNECT:
      Serial.printf("WebSocket client #%u disconnected\n", client->id());
      Stop(); // Stop the car when the WebSocket client disconnects
      break;

    case WS_EVT_DATA:
      AwsFrameInfo *info;
      info = (AwsFrameInfo*)arg;
      if (info->opcode == WS_TEXT && len == 1)
      {
        char command = data[0];
        Serial.printf("Received command: %c\n", command);
        switch (command)
        {
          case 'f':
          Serial.print("f"); 
            moveForward();
            break;

          case 'b':
          Serial.print("b");  
            moveBackward();
            break;

          case 'l':
          Serial.print("l");  
            moveLeft();
            break;

          case 'r':
          Serial.print("r");  
            moveRight();
            break;

          case 's': // Stop
            Serial.print("s");
            Stop();
            break;

          default:
            Serial.println("Unknown command");
            break;
        }
      }
      break;

    case WS_EVT_PONG:
    case WS_EVT_ERROR:
      break;

    default:
      break;
  }
}
void setupCamera()
{
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
}

void setupPins(){
   pinMode(in1, OUTPUT);
  pinMode(in2, OUTPUT);
  pinMode(enA, OUTPUT);
   pinMode(in3, OUTPUT);
  pinMode(in4, OUTPUT);
  pinMode(enB, OUTPUT);
}
void setup() {
  Serial.begin(115200);
   setupCamera();
  // Connect to Wi-Fi
  WiFi.begin(WIFI_SSID, WIFI_PASS);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to WiFi");
   Serial.print("ESP32 local IP: ");
  Serial.println(WiFi.localIP());

server.on("/stream", HTTP_GET, [](AsyncWebServerRequest *request){
 if (request->hasParam("stream")) {
      
    
      camera_fb_t *fb = NULL;

      while (request->client()->connected()) {
        fb = esp_camera_fb_get();
        if (!fb) {
          Serial.println("Camera capture failed!");
          continue;
        }
      String response = "--frame\r\nContent-Type: image/jpeg\r\n\r\n";
      response += (const char *)fb->buf;
      response += "\r\n";
       request->send(200, "multipart/x-mixed-replace; boundary=frame", response);

        esp_camera_fb_return(fb);
        delay(100);  // Adjust the delay as per the required frame rate
      }
    } else {
      request->send(400, "text/plain", "Invalid request");
    }
  });
   wsCarInput.onEvent(onCarInputWebSocketEvent);
  server.addHandler(&wsCarInput);
  server.begin();
  setupPins();
}
void moveForward(){

  digitalWrite(in1, HIGH);
  digitalWrite(in2, LOW);
   digitalWrite(in3, HIGH);
  digitalWrite(in4, LOW);
  analogWrite(enA, leftSpeed);
    analogWrite(enB, rightSpeed);

}
void moveLeft() {
  digitalWrite(in1, LOW);
  digitalWrite(in2, HIGH);
  digitalWrite(in3, HIGH);
  digitalWrite(in4, LOW);
  analogWrite(enA, leftSpeed / 2);
    analogWrite(enB, rightSpeed);
     delay(500); 
  Stop();
}

void moveRight() {
   digitalWrite(in1, HIGH);
  digitalWrite(in2, LOW);
  digitalWrite(in3, LOW);
  digitalWrite(in4, HIGH);
  analogWrite(enA, leftSpeed);
    analogWrite(enB, rightSpeed / 2);
     delay(500); 
  Stop();
}

void moveBackward() {
  digitalWrite(in1, LOW);
  digitalWrite(in2, HIGH);
   digitalWrite(in3, LOW);
  digitalWrite(in4, HIGH);
  analogWrite(enA, leftSpeed);
    analogWrite(enB, rightSpeed);
}

void Stop() {
  digitalWrite(in1, LOW);
  digitalWrite(in2, LOW);
  digitalWrite(in3, LOW);
  digitalWrite(in4, LOW);
  analogWrite(enA, 0);
  analogWrite(enB, 0);
}

void setSpeed(int newSpeed) {
  leftSpeed = constrain(newSpeed, 0, 255);
  rightSpeed = constrain(newSpeed, 0, 255);
}
void loop() {
 wsCarInput.cleanupClients();
}
