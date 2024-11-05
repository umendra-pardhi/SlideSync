import socket
import threading
import tkinter as tk
from tkinter import messagebox
import pyautogui

# Basic server settings
HOST = '0.0.0.0'
PORT = 5010
server_socket = None
server_running = False
android_connected = False  # Variable to track Android app connection status

# Function to get local IP
def get_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.settimeout(1)
    try:
        s.connect(("8.8.8.8", 80))
        IP = s.getsockname()[0]
    except Exception:
        IP = "Unavailable"
    finally:
        s.close()
    return IP

# Start the server
def start_server():
    global server_socket, server_running
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((HOST, PORT))
    server_socket.listen(1)
    server_running = True
    threading.Thread(target=accept_connections).start()
    start_button.config(state='disabled')
    stop_button.config(state='normal')
    ip_label.config(text=f"IP Address: {get_ip()}")
    server_status_label.config(text="Server Status: Running", fg="green")

# Stop the server
def stop_server():
    global server_socket, server_running
    server_running = False
    if server_socket:
        server_socket.close()
    start_button.config(state='normal')
    stop_button.config(state='disabled')
    server_status_label.config(text="Server Status: Stopped", fg="red")
    connectivity_status_label.config(text="Android Connectivity: Disconnected", fg="red")

def on_closing():
    if messagebox.askokcancel("Quit", "Do you want to quit?\nServer will Stop"):
        stop_server()
        app.destroy()

# Accept and handle connections
def accept_connections():
    global server_socket, android_connected
    while server_running:
        try:
            client_socket, addr = server_socket.accept()
            android_connected = True
            print(f"Connection from {addr}")
            connectivity_status_label.config(text="Android Connectivity: Connected", fg="green")
            threading.Thread(target=handle_client, args=(client_socket,)).start()
        except OSError:
            break

# Handle commands from client
def handle_client(client_socket):
    global android_connected
    while server_running:
        try:
            data = client_socket.recv(1024).decode('utf-8')
            if not data:
                break
            if data == "right":
                pyautogui.press('right')
            elif data == "left":
                pyautogui.press('left')
            elif data == "up":
                pyautogui.press('up')
            elif data == "down":
                pyautogui.press('down')
            elif data == "f5":
                pyautogui.press('f5')
            elif data == "shiftf5":
                pyautogui.hotkey('shift', 'f5')
            elif data == "home":
                pyautogui.press('home')
            elif data == "end":
                pyautogui.press('end')
            elif data == "esc":
                pyautogui.press('esc')
        except (ConnectionResetError, ConnectionAbortedError):
            break
    client_socket.close()
    android_connected = False
    connectivity_status_label.config(text="Android Connectivity: Disconnected", fg="red")

# Tkinter GUI setup
app = tk.Tk()
app.title("SlideSync Server")
app.geometry("500x250") 
app.iconbitmap('slidesync_logo.ico')


app.protocol("WM_DELETE_WINDOW", on_closing)

separator = tk.Canvas(app, height=1, bg="lightgray", highlightthickness=0)
separator.pack(side="top", fill="x")

# IP Address Label
ip_label = tk.Label(app, text=f"IP Address: {get_ip()}", font=("Arial", 12))
ip_label.pack(pady=10)

# Start Button
start_button = tk.Button(app, text="Start Server", command=start_server, font=("Arial", 10))
start_button.pack(pady=5)

# Stop Button (Initially Disabled)
stop_button = tk.Button(app, text="Stop Server", command=stop_server, font=("Arial", 10), state='disabled')
stop_button.pack(pady=5)

# Status Bar for Server and Android Connectivity

status_frame = tk.Frame(app,bg="white")
status_frame.pack(side="bottom", fill="x")

separator = tk.Canvas(app, height=1, bg="lightgray", highlightthickness=0)
separator.pack(side="bottom", fill="x")

dev_frame = tk.Frame(app)
dev_frame.pack(side="bottom", fill="x")

server_status_label = tk.Label(status_frame, text="Server Status: Stopped", font=("Arial", 10), fg="red", anchor="w",bg="white" )
server_status_label.pack(side="left", padx=10,pady=2)

connectivity_status_label = tk.Label(status_frame, text="Android Connectivity: Disconnected", font=("Arial", 10), fg="red", anchor="e",bg="white")
connectivity_status_label.pack(side="right", padx=10,pady=2)

dev_label = tk.Label(dev_frame, text="Developed by Umendra Pardhi", font=("Arial", 8))
dev_label.pack(pady=2)

# Run the app
app.mainloop()
