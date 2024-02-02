import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class PC2 extends JFrame {
    private static final int receiverPort = 12345;

    private final JTextArea messagesArea;
    private final JTextField messageField;
    private final JButton sendButton;
    private final JButton sendFileButton;
    private final DatagramSocket socket;
    private DatagramPacket lastReceivePacket;
    private int receivedMessagesCount = 0;
    private int sentMessagesCount = 0;
    private final JLabel receivedMessagesLabel;
    private final JLabel sentMessagesLabel;
    private final JProgressBar progressBar;

    private XYSeries azimuthData;
    private ChartPanel azimuthChartPanel;

    private XYSeries elevationData;
    private ChartPanel elevationChartPanel;

    private XYSeries distanceData;
    private ChartPanel distanceChartPanel;

    private class ImagePanel extends JPanel {
        private BufferedImage background;

        public ImagePanel(String imagePath) {
            try {
                background = ImageIO.read(new File(imagePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (background != null) {
                g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public PC2() throws Exception {
        socket = new DatagramSocket(receiverPort);

        messagesArea = new JTextArea(10, 30);
        messagesArea.setEditable(false);
        messagesArea.setFont(new Font("Century Gothic", Font.BOLD, 20));
        JScrollPane scrollPane = new JScrollPane(messagesArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        messageField = new JTextField(20);

        ImageIcon sendIcon = new ImageIcon("C:/Users/KIIT/Downloads/send_10322482.png");
        Image scaledSendIcon = sendIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        sendIcon = new ImageIcon(scaledSendIcon);
        sendButton = new JButton(sendIcon);

        sendFileButton = new JButton("Send Data");
        sendFileButton.addActionListener(e -> sendFile());

        receivedMessagesLabel = new JLabel("Received Messages: 0");
        sentMessagesLabel = new JLabel("Sent Messages: 0");

        sendButton.addActionListener(e -> sendResponse());

        ImagePanel topPanel = new ImagePanel("C:/Users/KIIT/Downloads/22489089_15199.jpg");
        topPanel.setLayout(new BorderLayout());

        ImageIcon logoIcon = new ImageIcon("C:/Users/KIIT/Downloads/Lgo1.jpg");
        Image scaledLogo = logoIcon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
        logoIcon = new ImageIcon(scaledLogo);
        JLabel logoLabel = new JLabel(logoIcon);
        topPanel.add(logoLabel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("   Tracking RADAR Trajectory Simulator");
        titleLabel.setFont(new Font("Bookman Old Style", Font.BOLD, 48));
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        counterPanel.add(receivedMessagesLabel);
        counterPanel.add(Box.createRigidArea(new Dimension(20, 0))); // Add some spacing
        counterPanel.add(sentMessagesLabel);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(counterPanel, BorderLayout.NORTH);

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(sendFileButton, BorderLayout.WEST);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        inputPanel.add(progressBar, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        azimuthData = new XYSeries("Azimuth Angle");
        elevationData = new XYSeries("Elevation Angle");
        distanceData = new XYSeries("Range");

        JFreeChart azimuthChart = ChartFactory.createXYLineChart(
                "Azimuth Angle",
                "Time (seconds)",
                "Azimuth Angle (Deg)",
                new XYSeriesCollection(azimuthData)
        );

        JFreeChart elevationChart = ChartFactory.createXYLineChart(
                "Elevation Angle",
                "Time (seconds)",
                "Elevation Angle (Deg)",
                new XYSeriesCollection(elevationData)
        );

        JFreeChart distanceChart = ChartFactory.createXYLineChart(
                "Range",
                "Time (seconds)",
                "Range (Km)",
                new XYSeriesCollection(distanceData)
        );

        customizeChartAppearance(azimuthChart);
        customizeChartAppearance(elevationChart);
        customizeChartAppearance(distanceChart);

        azimuthChartPanel = new ChartPanel(azimuthChart);
        elevationChartPanel = new ChartPanel(elevationChart);
        distanceChartPanel = new ChartPanel(distanceChart);

        azimuthChartPanel.setPreferredSize(new Dimension(400, 250));
        elevationChartPanel.setPreferredSize(new Dimension(400, 250));
        distanceChartPanel.setPreferredSize(new Dimension(400, 250));

        bottomPanel.add(azimuthChartPanel, BorderLayout.WEST);
        bottomPanel.add(elevationChartPanel, BorderLayout.CENTER);
        bottomPanel.add(distanceChartPanel, BorderLayout.EAST);

        setContentPane(mainPanel);

        setTitle("UDP Server");
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

        ValueAxis domainAxis = plot.getDomainAxis();
        ValueAxis rangeAxis = plot.getRangeAxis();
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        if (lastReceivePacket != null) {
                            byte[] fileData = readFile(selectedFile);

                            String fileContent = new String(fileData);
                            String[] lines = fileContent.split("\\r?\\n");

                            final int totalLines = lines.length;
                            final int[] sentLines = {0};

                            for (int i = 0; i < lines.length; i++) {
                                String[] data = lines[i].split(" ");

                                if (data.length >= 3) {
                                    double azimuth = Double.parseDouble(data[0]);
                                    double elevation = Double.parseDouble(data[1]);
                                    double distance = Double.parseDouble(data[2]);

                                    azimuthData.add(sentLines[0], azimuth);
                                    elevationData.add(sentLines[0], elevation);
                                    distanceData.add(sentLines[0], distance);
                                }

                                byte[] sendData = lines[i].getBytes();

                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                                        lastReceivePacket.getAddress(), lastReceivePacket.getPort());
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

                            SwingUtilities.invokeLater(() -> {
                                messagesArea.append("File sent to " + lastReceivePacket.getAddress() +
                                        ":" + lastReceivePacket.getPort() + ": " + selectedFile.getName() + "\n");
                                messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
                            });

                            sentMessagesCount++;
                            sentMessagesLabel.setText("Sent Messages: " + sentMessagesCount);
                        }
                    } catch (IOException | InterruptedException | NumberFormatException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };

            worker.execute();
        }
    }

    private byte[] readFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            return bos.toByteArray();
        }
    }

    private void sendResponse() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    if (lastReceivePacket != null) {
                        String message = messageField.getText();
                        byte[] sendData = message.getBytes();

                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                                lastReceivePacket.getAddress(), lastReceivePacket.getPort());
                        socket.send(sendPacket);

                        SwingUtilities.invokeLater(() -> {
                            messagesArea.append("Message sent to " + lastReceivePacket.getAddress() +
                                    ":" + lastReceivePacket.getPort() + ": " + message + "\n");
                            messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
                        });

                        messageField.setText("");

                        sentMessagesCount++;
                        sentMessagesLabel.setText("Sent Messages: " + sentMessagesCount);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        worker.execute();
    }

    private void receiveMessages() {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    while (true) {
                        byte[] receiveData = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        socket.receive(receivePacket);
                        lastReceivePacket = receivePacket;
                        String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                        publish(receivedMessage);

                        if (receivedMessage.equalsIgnoreCase("exit")) {
                            publish("Communication Terminated\n");
                            socket.close();
                            System.exit(0);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    messagesArea.append("Received Message: " + message + "\n");
                    messagesArea.setCaretPosition(messagesArea.getDocument().getLength());

                    String[] values = message.split("\\s+");
                    if (values.length >= 3) {
                        double azimuth = Double.parseDouble(values[0]);
                        double elevation = Double.parseDouble(values[1]);
                        double distance = Double.parseDouble(values[2]);

                        azimuthData.add(receivedMessagesCount, azimuth);
                        elevationData.add(receivedMessagesCount, elevation);
                        distanceData.add(receivedMessagesCount, distance);

                        receivedMessagesCount++;

                        receivedMessagesLabel.setText("Received Messages: " + receivedMessagesCount);

                        updateCharts();
                    }
                }
            }
        };

        worker.execute();
    }

    private void updateCharts() {
        // Update the charts as needed
        // You might need to add additional logic here based on your requirements
        // For example, you may want to limit the number of points displayed in the chart
        // or adjust the axis range dynamically.
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new PC2();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
