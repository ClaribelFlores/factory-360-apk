import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class VisorMoto360 extends JPanel {

    private static final String[] CARPETAS_MOTOS = {
        "moto1", "moto2", "moto3", "moto4", "moto5"
    };

    private static final String[] NOMBRES_MOTOS = {
        "Moto 1", "Moto 2", "Moto 3", "Moto 4", "Moto 5"
    };

    private static final String[] ARCHIVOS_FICHAS = {
        "ficha1.png",
        "ficha2.png",
        "ficha3.png",
        "ficha4.png",
        "ficha5.png"
    };

    private final List<Image> imagenes = new ArrayList<>();
    private final int indiceMoto;
    private int indiceActual = 0;
    private int mouseXInicial = 0;
    private static final int SENSIBILIDAD_PX = 18;

    private String motoActiva;
    private String mensajeEstado = "Arrastra hacia los lados para girar en 360°";
    private final JLabel lblInfo;

    public VisorMoto360(int indiceMoto, JLabel lblInfo) {
        this.indiceMoto = indiceMoto;
        this.lblInfo = lblInfo;

        setBackground(Color.BLACK);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseXInicial = e.getX();
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (imagenes.isEmpty()) return;

                int deltaX = e.getX() - mouseXInicial;
                if (Math.abs(deltaX) >= SENSIBILIDAD_PX) {
                    if (deltaX > 0) {
                        indiceActual = (indiceActual + 1) % imagenes.size();
                    } else {
                        indiceActual = (indiceActual - 1 + imagenes.size()) % imagenes.size();
                    }
                    mouseXInicial = e.getX();
                    actualizarEtiqueta();
                    repaint();
                }
            }
        });

        cargarMoto(this.indiceMoto);
    }

    private File resolverCarpetaBaseImagenes() {
        String[] rutasPosibles = {
            "imagenes",
            "VisorMotoJava" + File.separator + "imagenes",
            "src" + File.separator + "main" + File.separator + "resources" + File.separator + "imagenes"
        };

        for (String ruta : rutasPosibles) {
            File dir = new File(ruta);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }
        return new File("imagenes");
    }

    private File resolverArchivoFicha(int idxMoto) {
        if (idxMoto < 0 || idxMoto >= ARCHIVOS_FICHAS.length) return null;

        String nombreFicha = ARCHIVOS_FICHAS[idxMoto];
        File baseDir = resolverCarpetaBaseImagenes();

        File enBase = new File(baseDir, nombreFicha);
        if (enBase.exists()) {
            return enBase;
        }

        File enCarpetaMoto = new File(new File(baseDir, CARPETAS_MOTOS[idxMoto]), nombreFicha);
        if (enCarpetaMoto.exists()) {
            return enCarpetaMoto;
        }

        return new File(nombreFicha);
    }

    public void cargarMoto(int idxMoto) {
        if (idxMoto < 0 || idxMoto >= CARPETAS_MOTOS.length) return;

        String carpetaNombre = CARPETAS_MOTOS[idxMoto];
        motoActiva = NOMBRES_MOTOS[idxMoto];
        imagenes.clear();
        indiceActual = 0;

        File baseDir = resolverCarpetaBaseImagenes();
        File carpetaMoto = new File(baseDir, carpetaNombre);

        int numero = 1;
        while (true) {
            File archivo = new File(carpetaMoto, numero + ".png");
            if (!archivo.exists()) {
                break;
            }
            try {
                Image img = ImageIO.read(archivo);
                if (img != null) {
                    imagenes.add(img);
                } else {
                    System.err.println("No se pudo decodificar la imagen: " + archivo.getAbsolutePath());
                }
            } catch (IOException e) {
                System.err.println("No se encontro la imagen: " + archivo.getAbsolutePath());
                e.printStackTrace();
            }
            numero++;
        }

        if (imagenes.isEmpty()) {
            mensajeEstado = "No se encontraron imágenes en: " + carpetaMoto.getAbsolutePath();
            System.err.println(mensajeEstado);
        } else {
            mensajeEstado = "Arrastra hacia los lados para girar en 360°";
        }

        actualizarEtiqueta();
        repaint();
    }

    private void actualizarEtiqueta() {
        if (lblInfo == null) return;
        if (imagenes.isEmpty()) {
            lblInfo.setText(motoActiva + " | Sin imágenes cargadas");
        } else {
            lblInfo.setText(motoActiva + "  —  Ángulo " + (indiceActual + 1) + " de " + imagenes.size());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (imagenes.isEmpty()) {
            g2.setColor(new Color(230, 57, 70));
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString(mensajeEstado, 30, getHeight() / 2);
            return;
        }

        Image imgActual = imagenes.get(indiceActual);
        int anchoPanel = getWidth() - 24;
        int altoPanel = getHeight() - 24;

        int imgAncho = imgActual.getWidth(this);
        int imgAlto = imgActual.getHeight(this);

        if (imgAncho > 0 && imgAlto > 0) {
            double escala = Math.min((double) anchoPanel / imgAncho, (double) altoPanel / imgAlto);
            int drawW = (int) (imgAncho * escala);
            int drawH = (int) (imgAlto * escala);
            int drawX = (getWidth() - drawW) / 2;
            int drawY = (getHeight() - drawH) / 2;

            g2.drawImage(imgActual, drawX, drawY, drawW, drawH, this);
        }
    }

    public void mostrarFichaTecnica(JFrame ventanaPadre) {
        File archivoFicha = resolverArchivoFicha(indiceMoto);
        if (archivoFicha == null || !archivoFicha.exists()) {
            JOptionPane.showMessageDialog(
                ventanaPadre,
                "No se encontró la ficha técnica para " + NOMBRES_MOTOS[indiceMoto] + ".\nBuscada en: " +
                (archivoFicha != null ? archivoFicha.getAbsolutePath() : "ruta no válida"),
                "Ficha técnica no encontrada",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            Image imgFicha = ImageIO.read(archivoFicha);
            if (imgFicha == null) {
                JOptionPane.showMessageDialog(
                    ventanaPadre,
                    "No se pudo leer la imagen de la ficha técnica: " + archivoFicha.getName(),
                    "Error de imagen",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JDialog dialogoFicha = new JDialog(ventanaPadre, "Ficha Técnica - " + NOMBRES_MOTOS[indiceMoto], true);
            dialogoFicha.setSize(980, 740);
            dialogoFicha.setLocationRelativeTo(ventanaPadre);
            dialogoFicha.setLayout(new BorderLayout(0, 0));
            dialogoFicha.getContentPane().setBackground(Color.BLACK);

            JPanel panelFicha = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    int margen = 8;
                    int dispW = Math.max(1, getWidth() - margen * 2);
                    int dispH = Math.max(1, getHeight() - margen * 2);

                    int w = imgFicha.getWidth(this);
                    int h = imgFicha.getHeight(this);
                    if (w > 0 && h > 0) {
                        double escala = Math.min((double) dispW / w, (double) dispH / h);
                        int drawW = Math.max(1, (int) Math.round(w * escala));
                        int drawH = Math.max(1, (int) Math.round(h * escala));
                        int x = (getWidth() - drawW) / 2;
                        int y = (getHeight() - drawH) / 2;
                        g2.drawImage(imgFicha, x, y, drawW, drawH, this);
                    }
                }
            };
            panelFicha.setBackground(Color.BLACK);

            JButton btnCerrar = new JButton("Cerrar");
            btnCerrar.setFocusPainted(false);
            btnCerrar.setBackground(new Color(225, 29, 72));
            btnCerrar.setForeground(Color.WHITE);
            btnCerrar.setFont(new Font("Arial", Font.BOLD, 13));
            btnCerrar.addActionListener(e -> dialogoFicha.dispose());

            JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            pie.setBackground(new Color(18, 18, 18));
            pie.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(45, 45, 45)));
            pie.add(btnCerrar);

            dialogoFicha.add(panelFicha, BorderLayout.CENTER);
            dialogoFicha.add(pie, BorderLayout.SOUTH);
            dialogoFicha.setVisible(true);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                ventanaPadre,
                "Error al cargar la ficha técnica: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void abrirVentanaSeparada() {
        JFrame ventanaMoto = new JFrame("Visor 360° - " + NOMBRES_MOTOS[indiceMoto]);
        ventanaMoto.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventanaMoto.setSize(820, 620);
        ventanaMoto.setLocationRelativeTo(null);
        ventanaMoto.setLayout(new BorderLayout());
        ventanaMoto.getContentPane().setBackground(Color.BLACK);

        JLabel lblEstado = new JLabel("", JLabel.CENTER);
        lblEstado.setForeground(Color.WHITE);
        lblEstado.setFont(new Font("Arial", Font.BOLD, 14));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        VisorMoto360 panelVisor = new VisorMoto360(indiceMoto, lblEstado);

        JButton btnFicha = new JButton("Ver ficha técnica");
        btnFicha.setFocusPainted(false);
        btnFicha.setBackground(new Color(225, 29, 72));
        btnFicha.setForeground(Color.WHITE);
        btnFicha.setFont(new Font("Arial", Font.BOLD, 13));
        btnFicha.addActionListener(e -> panelVisor.mostrarFichaTecnica(ventanaMoto));

        JPanel panelInferior = new JPanel(new BorderLayout(10, 0));
        panelInferior.setBackground(new Color(18, 18, 18));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        panelInferior.add(lblEstado, BorderLayout.CENTER);
        panelInferior.add(btnFicha, BorderLayout.EAST);

        ventanaMoto.add(panelVisor, BorderLayout.CENTER);
        ventanaMoto.add(panelInferior, BorderLayout.SOUTH);
        ventanaMoto.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame catalogoFrame = new JFrame("Concesionaria - Catálogo de Motos");
            catalogoFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            catalogoFrame.setSize(940, 580);
            catalogoFrame.setLocationRelativeTo(null);
            catalogoFrame.setLayout(new BorderLayout(15, 15));
            catalogoFrame.getContentPane().setBackground(Color.BLACK);

            JLabel titulo = new JLabel("Elige una moto para abrir su visor 360° o consultar su ficha técnica", SwingConstants.CENTER);
            titulo.setFont(new Font("Arial", Font.BOLD, 18));
            titulo.setForeground(Color.WHITE);
            titulo.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));

            JPanel gridMotos = new JPanel(new GridLayout(2, 3, 15, 15));
            gridMotos.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
            gridMotos.setBackground(Color.BLACK);

            VisorMoto360 buscadorRuta = new VisorMoto360(0, null);
            File baseDir = buscadorRuta.resolverCarpetaBaseImagenes();

            for (int i = 0; i < CARPETAS_MOTOS.length; i++) {
                final int idx = i;
                JPanel tarjeta = new JPanel(new BorderLayout(8, 8));
                tarjeta.setBackground(new Color(22, 22, 22));
                tarjeta.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));

                JLabel lblImagen = new JLabel("", SwingConstants.CENTER);
                lblImagen.setForeground(Color.LIGHT_GRAY);
                File portada = new File(new File(baseDir, CARPETAS_MOTOS[i]), "1.png");
                if (portada.exists()) {
                    ImageIcon icon = new ImageIcon(portada.getAbsolutePath());
                    Image escalada = icon.getImage().getScaledInstance(220, 130, Image.SCALE_SMOOTH);
                    lblImagen.setIcon(new ImageIcon(escalada));
                } else {
                    lblImagen.setText("Sin vista previa");
                }

                JButton btnAbrir = new JButton("Abrir en 360°");
                btnAbrir.setFocusPainted(false);
                btnAbrir.setBackground(new Color(45, 45, 45));
                btnAbrir.setForeground(Color.WHITE);
                btnAbrir.addActionListener(e -> {
                    VisorMoto360 visorIndependiente = new VisorMoto360(idx, null);
                    visorIndependiente.abrirVentanaSeparada();
                });

                JButton btnFichaTarjeta = new JButton("Ficha técnica");
                btnFichaTarjeta.setFocusPainted(false);
                btnFichaTarjeta.setBackground(new Color(225, 29, 72));
                btnFichaTarjeta.setForeground(Color.WHITE);
                btnFichaTarjeta.addActionListener(e -> {
                    VisorMoto360 referenciaMoto = new VisorMoto360(idx, null);
                    referenciaMoto.mostrarFichaTecnica(catalogoFrame);
                });

                JLabel lblNombre = new JLabel(NOMBRES_MOTOS[i], SwingConstants.CENTER);
                lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
                lblNombre.setForeground(Color.WHITE);
                lblNombre.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

                tarjeta.add(lblNombre, BorderLayout.NORTH);
                tarjeta.add(lblImagen, BorderLayout.CENTER);

                JPanel pieTarjeta = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
                pieTarjeta.setBackground(new Color(22, 22, 22));
                pieTarjeta.add(btnAbrir);
                pieTarjeta.add(btnFichaTarjeta);
                tarjeta.add(pieTarjeta, BorderLayout.SOUTH);

                gridMotos.add(tarjeta);
            }

            catalogoFrame.add(titulo, BorderLayout.NORTH);
            catalogoFrame.add(gridMotos, BorderLayout.CENTER);
            catalogoFrame.setVisible(true);
        });
    }
}