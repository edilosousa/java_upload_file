import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class App {
    public static void main(String[] args) {
        // Criar a janela principal
        JFrame frame = new JFrame("Salvar Arquivo no Banco e Fazer Upload");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        // Campo de exibição do caminho do arquivo
        JTextField txtFilePath = new JTextField(30);
        txtFilePath.setEditable(false);

        // Botão para selecionar arquivo
        JButton btnSelectFile = new JButton("Selecionar Arquivo");

        // Botão para salvar no banco
        JButton btnSaveToDb = new JButton("Salvar no Banco e Fazer Upload");

        // Adicionar ação ao botão de selecionar arquivo
        btnSelectFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    txtFilePath.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        // Adicionar ação ao botão de salvar no banco
        btnSaveToDb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = txtFilePath.getText();
                if (filePath.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Por favor, selecione um arquivo primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Caminho de upload no projeto
                String uploadDir = "uploads"; // Pasta dentro do projeto
                File uploadFolder = new File(uploadDir);
                if (!uploadFolder.exists()) {
                    uploadFolder.mkdir(); // Criar a pasta se não existir
                }

                File sourceFile = new File(filePath);
                File destinationFile = new File(uploadDir + File.separator + sourceFile.getName());

                // Realizar o upload
                try {
                    Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Dados de conexão
                    String url = "jdbc:postgresql://localhost:5432/postgres";
                    String user = "postgres";
                    String password = "postgres";

                    try (Connection conn = DriverManager.getConnection(url, user, password)) {
                        if (conn != null) {
                            // Inserir no banco
                            String sql = "INSERT INTO public.tblfile (file_caminho, file_data) VALUES (?, NOW())";
                            PreparedStatement stmt = conn.prepareStatement(sql);
                            stmt.setString(1, destinationFile.getAbsolutePath());
                            int rows = stmt.executeUpdate();

                            if (rows > 0) {
                                JOptionPane.showMessageDialog(frame, "Arquivo salvo e upload realizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Erro ao salvar no banco: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Erro ao fazer o upload do arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Adicionar componentes à janela
        frame.add(txtFilePath);
        frame.add(btnSelectFile);
        frame.add(btnSaveToDb);

        // Exibir a janela
        frame.setVisible(true);
    }
}
