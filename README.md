# usb-serial

Simple Android application that enables serial communication with USB-to-Serial (UART) devices, such as devices using Silicon Labs CP210x chips. You can send and receive messages between your Android phone and an external device using a USB connection.

## 📱 Features

- Send and receive serial data via USB.
- Display received messages.
- Compatible with CP2102, FTDI, and other common UART-USB converters.
- Basic GUI with text input and display.

---

## ⚙️ Requirements

- Android device with USB OTG support.
- USB-to-Serial converter (e.g., CP2102).
- External device (e.g., 2N Lift8, Arduino, STM32, etc.).
- Optional: PC with PuTTY for testing communication.

---

## 🚀 How to Use

### 1. **Connect the USB Serial Device**

Plug your USB-to-Serial adapter (e.g., CP2102) into your Android device using a USB OTG cable.

### 2. **Run the App**

When launched:
- It will ask for USB permission.
- If successful, you can start sending and receiving messages.

### 3. **Send a Message**

Type your message in the text field and press the **Send** button. You’ll see your message appear below along with any response from the serial device.

---

## 💻 Optional: Communicate with PC (PuTTY)

To test communication, you can use PuTTY on a Windows PC:

### **Step 1: Identify COM Port**

1. Open **Device Manager**.
2. Find your USB-UART adapter under `Ports (COM & LPT)`.

![image](https://github.com/user-attachments/assets/b1238e5f-ff14-4bb8-bc04-50f3530e90ed)



In this example, the port is `COM9`.

---

### **Step 2: Configure PuTTY**

1. Open PuTTY.
2. Choose **Serial** as the connection type.
3. Set the serial line to match your port (e.g., `COM9`).
4. Set the speed to `115200` (must match your Android app settings).

![image](https://github.com/user-attachments/assets/9944bb6a-fc7f-4771-919a-d62ed02d3ea0)


5. Click **Open** to begin the session.

Now, messages sent from the Android app should appear in PuTTY and vice versa.

---

## 🧪 Screenshots

### Android App Main Screen
![image](https://github.com/user-attachments/assets/e890d462-bf29-4431-9d6a-2694d22a00c8)


---
