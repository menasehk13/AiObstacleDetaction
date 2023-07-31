import cv2

# Replace with the URL of the video stream
video_url = "http://example.com/stream"

# Create VideoCapture object
cap = cv2.VideoCapture(video_url)

# Check if the video stream was opened successfully
if not cap.isOpened():
    print("Error: Could not open the video stream.")
    exit()

while True:
    # Read frame from the video stream
    ret, frame = cap.read()

    # Check if the frame was successfully read
    if not ret:
        print("Error: Could not read frame.")
        break

    # Display the frame
    cv2.imshow("Video Stream", frame)

    # Exit the loop if 'q' key is pressed
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# Release the VideoCapture and close the OpenCV windows
cap.release()
cv2.destroyAllWindows()
