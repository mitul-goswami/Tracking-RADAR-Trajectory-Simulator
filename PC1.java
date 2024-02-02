import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

class BackgroundPanel extends JPanel {
    private ImageIcon backgroundImage;

    public BackgroundPanel(String imagePath) {
        setBackgroundImage(imagePath);
    }

    public void setBackgroundImage(String imagePath) {
        this.backgroundImage = new ImageIcon(imagePath);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
    }
}

public class PC1 extends JFrame {
    private static final String receiverIP = "10.2.101.234";
    private static final int receiverPort = 12345;

    private final JTextArea messagesArea;
    private final JTextField messageField;
    private final JTextField inputField;
    private final JButton sendButton;
    private final JButton sendFileButton;
    private final DatagramSocket socket;
    private int sentMessagesCount = 0;
    private int receivedMessagesCount = 0;
    private final JLabel sentMessagesLabel;
    private final JLabel receivedMessagesLabel;
    private JFileChooser fileChooser;
    private JPanel inputPanel;
    private final JProgressBar progressBar;

    private XYSeries chartData;
    private XYSeries azimuthData;
    private XYSeries elevationData;
    private XYSeries distanceData;
    private ChartPanel chartPanel;
    private ChartPanel azimuthChartPanel;
    private ChartPanel elevationChartPanel;
    private ChartPanel distanceChartPanel;

    public PC1() throws Exception {
        socket = new DatagramSocket();
        fileChooser = new JFileChooser();
        chartData = new XYSeries("Data Transmission Progress");
        azimuthData = new XYSeries("Azimuth Angle");
        elevationData = new XYSeries("Elevation Angle");
        distanceData = new XYSeries("Range");
        progressBar = new JProgressBar();
        messagesArea = new JTextArea(20, 20);
        messagesArea.setEditable(false);
        messagesArea.setFont(new Font("Century Gothic", Font.BOLD, 20));
        JScrollPane scrollPane = new JScrollPane(messagesArea);

        messageField = new JTextField(40);
        inputField = new JTextField(50);

        ImageIcon sendIcon = new ImageIcon("C:/Users/KIIT/Downloads/send_10322482.png");
        Image scaledSendIcon = sendIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        sendIcon = new ImageIcon(scaledSendIcon);
        sendButton = new JButton(sendIcon);

        sendFileButton = new JButton("Send Data");

        sendButton.addActionListener(e -> sendMessage());
        sendFileButton.addActionListener(e -> sendFile());

        ImageIcon mainLogoIcon = new ImageIcon("C:/Users/KIIT/Downloads/Lgo1.jpg");
        Image scaledMainLogo = mainLogoIcon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
        mainLogoIcon = new ImageIcon(scaledMainLogo);
        JLabel mainLogoLabel = new JLabel(mainLogoIcon);

        BackgroundPanel topPanel = new BackgroundPanel("C:/Users/KIIT/Downloads/22489089_15199.jpg");
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        Font titleFont = new Font("Bookman Old Style", Font.BOLD, 50);
        JTextField titleLabel = new JTextField(" Tracking RADAR Trajectory Simulator");
        titleLabel.setFont(titleFont);
        titleLabel.setEditable(false);
        titleLabel.setBorder(BorderFactory.createEmptyBorder());
        titleLabel.setOpaque(false);

        topPanel.add(mainLogoLabel);
        topPanel.add(titleLabel);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "File Transmission Progress",
                "Time (seconds)",
                "Lines Sent",
                new XYSeriesCollection(chartData),
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        JFreeChart azimuthChart = ChartFactory.createXYLineChart(
                "Azimuth Angle",
                "Time (seconds)",
                "Azimuth Angle (Deg)",
                new XYSeriesCollection(azimuthData),
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        JFreeChart elevationChart = ChartFactory.createXYLineChart(
                "Elevation Angle",
                "Time (seconds)",
                "Elevation Angle (Deg)",
                new XYSeriesCollection(elevationData),
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        JFreeChart distanceChart = ChartFactory.createXYLineChart(
                "Range",
                "Time (seconds)",
                "Range (Km)",
                new XYSeriesCollection(distanceData),
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChartAppearance(chart);
        customizeChartAppearance(azimuthChart);
        customizeChartAppearance(elevationChart);
        customizeChartAppearance(distanceChart);

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 250));

        azimuthChartPanel = new ChartPanel(azimuthChart);
        azimuthChartPanel.setPreferredSize(new Dimension(400, 250));

        elevationChartPanel = new ChartPanel(elevationChart);
        elevationChartPanel.setPreferredSize(new Dimension(400, 250));

        distanceChartPanel = new ChartPanel(distanceChart);
        distanceChartPanel.setPreferredSize(new Dimension(400, 250));

        BackgroundPanel terminalPanel = new BackgroundPanel("C:/Users/KIIT/Downloads/terminal_background.jpg");
        terminalPanel.setLayout(new BorderLayout());

        JPanel chartsPanel = new JPanel(new GridLayout(1, 3));
        chartsPanel.add(azimuthChartPanel);
        chartsPanel.add(elevationChartPanel);
        chartsPanel.add(distanceChartPanel);

        terminalPanel.add(scrollPane, BorderLayout.CENTER);
        terminalPanel.add(chartsPanel, BorderLayout.SOUTH);

        inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(sendFileButton, BorderLayout.WEST);
        inputPanel.add(progressBar, BorderLayout.SOUTH);

        sentMessagesLabel = new JLabel("Sent Messages: 0");
        receivedMessagesLabel = new JLabel("Received Messages: 0");

        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        counterPanel.add(sentMessagesLabel);
        counterPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        counterPanel.add(receivedMessagesLabel);

        inputPanel.add(counterPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(terminalPanel, BorderLayout.CENTER);
        mainPanel.add(messageField, BorderLayout.SOUTH);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        setTitle("UDP Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        receiveMessages();
    }

    private void customizeChartAppearance(JFreeChart chart) {
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();

        float thickness = 2.5f;
        BasicStroke stroke = new BasicStroke(thickness);

        for (int i = 0; i < plot.getDatasetCount(); i++) {
            renderer.setSeriesStroke(i, stroke);
        }
    }

    private void sendMessage() {
        try {
            String message = inputField.getText();
            byte[] sendData = message.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                    InetAddress.getByName(receiverIP), receiverPort);

            socket.send(sendPacket);

            if (message.equalsIgnoreCase("exit")) {
                messagesArea.append("Communication Terminated\n");
                socket.close();
                System.exit(0);
            }

            messagesArea.append("Message sent to " + receiverIP + ":" + receiverPort + ": " + message + "\n");
            messagesArea.setCaretPosition(messagesArea.getDocument().getLength());

            inputField.setText("");

            sentMessagesCount++;
            sentMessagesLabel.setText("Sent Messages: " + sentMessagesCount);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFile() {
        int result = fileChooser.showOpenDialog(PC1.this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                // Read file content
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());

                // Send file content in a separate thread
                new Thread(() -> sendFileInBackground(fileData, selectedFile.getName())).start();

            } catch (IOException e) {
                e.printStackTrace();
                messagesArea.append("Error reading the file.\n");
            }
        }
    }

    private void sendFileInBackground(byte[] fileData, String fileName) {
        try {
            String fileContent = new String(fileData);
            String[] lines = fileContent.split("\\r?\\n");
            long startTime = System.currentTimeMillis();

            final int totalLines = lines.length;
            final int[] sentLines = {0};

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                byte[] sendData = line.getBytes();
                double elapsedTimeSeconds = (System.currentTimeMillis() - startTime) / 1000.0;

                chartData.add(elapsedTimeSeconds, sentLines[0]);

                String[] values = line.split("\\s+");
                if (values.length > 0) {
                    double azimuthAngle = Double.parseDouble(values[0]);
                    azimuthData.add(elapsedTimeSeconds, azimuthAngle);
                }

                if (values.length > 1) {
                    double elevationAngle = Double.parseDouble(values[1]);
                    elevationData.add(elapsedTimeSeconds, elevationAngle);
                }

                // Extract Distance Travelled from the third column of the line
                if (values.length > 2) {
                    double distanceTravelled = Double.parseDouble(values[2]);
                    distanceData.add(elapsedTimeSeconds, distanceTravelled);
                }

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName(receiverIP), receiverPort);

                socket.send(sendPacket);
                sentLines[0]++;

                SwingUtilities.invokeLater(() -> {
                    int progress = (int) ((double) sentLines[0] / totalLines * 100);
                    progressBar.setValue(progress);
                    progressBar.setString("Sending: " + progress + "%");

                    if (sentLines[0] == totalLines) {
                        progressBar.setValue(100);
                        progressBar.setString("Sending: 100%");
                    }
                });

                Thread.sleep(1000);
            }

            messagesArea.append("RADAR Data sent to " + receiverIP + ":" + receiverPort + ": " + fileName + "\n");
            messagesArea.setCaretPosition(messagesArea.getDocument().getLength());

            sentMessagesCount++;
            sentMessagesLabel.setText("Sent Messages: " + sentMessagesCount);

        } catch (IOException | InterruptedException | NumberFormatException e) {
            e.printStackTrace();
            messagesArea.append("Error sending the file.\n");
        }
    }

    private void receiveMessages() {
        // Existing receiveMessages() method
        new Thread(() -> {
            try {
                while (true) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    SwingUtilities.invokeLater(() -> {
                        messagesArea.append("Received Response: " + receivedMessage + "\n");
                        messagesArea.setCaretPosition(messagesArea.getDocument().getLength());

                        if (receivedMessage.equalsIgnoreCase("exit")) {
                            messagesArea.append("Communication Terminated\n");
                            socket.close();
                            System.exit(0);
                        }

                        receivedMessagesCount++;
                        receivedMessagesLabel.setText("Received Messages: " + receivedMessagesCount);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new PC1();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
