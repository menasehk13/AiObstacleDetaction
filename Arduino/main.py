import cv2
import threading
import asyncio
import websockets

# Replace with the URL of the video stream
video_url = "http://example.com/stream"

# Replace with the WebSocket server URL
websocket_server_url = "ws://example.com/ws"

# Global variable to store the WebSocket connection
websocket = None

def receive_keyboard_input():
    global websocket

    while True:
        # Read input from the keyboard
        user_input = input("Enter data (f=forward, l=left, b=backward, r=right, s=stop): ")

        # Send the data to the WebSocket server if the connection is established
        if websocket:
            asyncio.run(send_command(user_input))

async def send_command(command):
    global websocket
    if websocket:
        await websocket.send(command)
        # Receive response from the WebSocket server (optional)
        response = await websocket.recv()
        print(f"Received from server: {response}")

def receive_video_stream():
    # Create VideoCapture object
    cap = cv2.VideoCapture(video_url)

    # Check if the video stream was opened successfully
    if not cap.isOpened():
        print("Error: Could not open the video stream.")
        return

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

async def connect_websocket():
    global websocket
    try:
        websocket = await websockets.connect(websocket_server_url)
        # Start receiving commands from the WebSocket server
        while True:
            response = await websocket.recv()
            print(f"Received from server: {response}")
    except Exception as e:
        print(f"Error: {e}")
        # In case of an error, set the WebSocket connection to None
        websocket = None

# Create and start threads for video stream and WebSocket communication
video_thread = threading.Thread(target=receive_video_stream)
websocket_thread = threading.Thread(target=asyncio.run, args=(connect_websocket(),))

video_thread.start()
websocket_thread.start()

# Wait for both threads to finish
video_thread.join()
websocket_thread.join()
