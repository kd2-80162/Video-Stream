package com.pract;

import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Camera extends JFrame {

    private JLabel cameraScreen;
    private JButton btnCapture;
    private VideoCapture capture;
    private Mat frame;
    private CascadeClassifier faceDetector;
    private boolean isRunning;

    public Camera() {
        // Load the Haarcascade file
        String haarcascadePath = "C:/path/to/haarcascade_frontalface_default.xml";
        faceDetector = new CascadeClassifier(haarcascadePath);

        if (faceDetector.empty()) {
            System.err.println("Error: Could not load classifier cascade.");
            return;
        }

        // Design UI
        setLayout(null);
        cameraScreen = new JLabel();
        cameraScreen.setBounds(0, 0, 640, 480);
        add(cameraScreen);

        btnCapture = new JButton("Capture");
        btnCapture.setBounds(300, 480, 80, 40);
        add(btnCapture);

        setSize(new Dimension(640, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // Initialize video capture and frame
        capture = new VideoCapture(0);
        frame = new Mat();
        isRunning = true;

        // Start the video capture thread
        new Thread(() -> {
            while (isRunning) {
                if (capture.read(frame)) {
                    detectAndDisplay(frame);
                }
            }
        }).start();
    }

    private void detectAndDisplay(Mat frame) {
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(frame, faceDetections);

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(frame, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));
        }

        // Update the camera screen with the processed frame
        cameraScreen.setIcon(new ImageIcon(Mat2BufferedImage(frame)));
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("OpenCV loaded successfully");
        
        EventQueue.invokeLater(() -> {
            new Camera();
        });
    }

    // Convert Mat object to BufferedImage for display
    private BufferedImage Mat2BufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer); // Get all the pixels
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }
}
