import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import vpt.Image;
import vpt.algorithms.display.Display2D;
import vpt.algorithms.io.Load;
import vpt.algorithms.io.Save;

public class ImageConvolutionTest {
    private JFrame frame;
    private JPanel gridP;
    private int kernelSize;
    private JTextField[][] grids;
    private JTextField sizeT;
    private JTextField divT;
    private JLabel divLabel;
    private JTextField imgPath;
    Image origImg;
    Image maskedImg;
    double[][] kernel;
    double divisor;

    public static void main(String[] args){
        new ImageConvolutionTest();
    }
    
    public ImageConvolutionTest(){
        
        init();
    }
    
    private void init(){
        
        frame =new JFrame();
        frame.setTitle("Image Convolution");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,320);
        frame.setVisible(true);
        
        JPanel startP =new JPanel();
        startP.setSize(frame.getWidth(), frame.getHeight());
        startP.setLayout(null);
        startP.setBackground(new Color(104,165,236));
        
        gridP =new JPanel();
        gridP.setBackground(Color.BLACK);
        gridP.setSize(120,120);
        gridP.setLocation(150,150);

        JLabel sizeLabel =new JLabel();
        sizeLabel.setText("Kernel Size :");
        sizeLabel.setBounds(30, 70, 100, 30);
        
        sizeT =new JTextField();
        sizeT.setText("3");
        sizeT.setBounds(130, 70, 30, 30);
        sizeT.addActionListener(new KernelInput());
        
        JButton kernelB =new JButton();
        kernelB.setText("Create Kernel");
        kernelB.setBounds(170, 70, 140, 30);
        kernelB.addActionListener(new KernelInput());
        
        divLabel =new JLabel();
        divLabel.setText("Divisor :");
        divLabel.setBounds(30,130+gridP.getHeight()/2,75,30);
        
        divT =new JTextField();
        divT.setText("1");
        divT.setBounds(100,130+gridP.getHeight()/2,30,30);
        divT.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                double div =Double.parseDouble(divT.getText());
                if(div==0.0){
                    JOptionPane.showMessageDialog(frame,"Divisor number can not be 0 !!!","Warning!!!",JOptionPane.WARNING_MESSAGE);
                    divT.setText(Double.toString(1.0));
                }
            }
        });
        
        JButton applyB =new JButton();
        applyB.setText("Apply Convolution");
        applyB.setBounds(320, 70, 170, 30);
        applyB.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                origImg =Load.invoke(imgPath.getText());               
                if(origImg !=null){
                    setKernel();
                    maskedImg =ImageConvolution.convolution(origImg, kernel,divisor);
                    Display2D.invoke(maskedImg,"Masked Image With Convolution");
                }
            }
        });
        
        JButton applyFB =new JButton();
        applyFB.setText("Apply With Fourier");
        applyFB.setBounds(320,110,170,30);
        applyFB.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                origImg =Load.invoke(imgPath.getText());               
                if(origImg !=null){
                    setKernel();
                    maskedImg =ImageConvolution.convolutionWithFourier(origImg, kernel,divisor);
                    Display2D.invoke(maskedImg,"Masked Image With Fourier");
                }
            }
        });
        
        JButton saveB =new JButton();
        saveB.setText("Save");
        saveB.setBounds(500,70,90,30);
        saveB.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(); 
                chooser.setCurrentDirectory(new File("."));
                chooser.setDialogTitle("Choose Directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (maskedImg !=null && chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File file =new File(chooser.getSelectedFile().getAbsolutePath());
                    if(!file.isDirectory()){
                        Save.invoke(maskedImg,chooser.getSelectedFile().getAbsolutePath());
                    }
                    else{
                        JOptionPane.showMessageDialog(frame,"You did not give a name to your file!!!\nAssigned as default maskedImage.png",
                                "Warning!!!",JOptionPane.WARNING_MESSAGE);
                        Save.invoke(maskedImg,chooser.getSelectedFile().getAbsolutePath()+"/maskedImage.png");
                    }
                }
            }
        
        });
        
        JButton chooseB =new JButton();
        chooseB.setText("Choose Image");
        chooseB.setBounds(50,25, 150, 30);
        chooseB.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
         
                JFileChooser chooser = new JFileChooser(); 
                chooser.setCurrentDirectory(new File("."));
                chooser.setDialogTitle("Choose Image");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    imgPath.setText(chooser.getSelectedFile().getAbsolutePath());
                    origImg =Load.invoke(imgPath.getText());
                    if(origImg !=null){
                        Display2D.invoke(origImg, "Original Image");
                    }
                }
            }
        });
        
        imgPath =new JTextField();
        imgPath.setText("Image path:");
        imgPath.setBounds(220, 25, 350, 30);
        imgPath.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                origImg =Load.invoke(imgPath.getText());
                if(origImg !=null){
                    Display2D.invoke(origImg, "Original Image");
                }
            }
        });
        
        
        origImg =null;
        kernelSize =3;
        
        startP.add(chooseB);
        startP.add(sizeT);
        startP.add(sizeLabel);
        startP.add(kernelB);
        startP.add(imgPath);
        startP.add(applyB);
        startP.add(divLabel);
        startP.add(divT);
        startP.add(gridP);
        startP.add(saveB);
        startP.add(applyFB);
        setGridPanel();
        
        frame.add(startP);
                
        frame.revalidate();
        frame.repaint();
    }
    
    private void setGridPanel(){
        gridP.removeAll();
        gridP.setLayout(new GridLayout(kernelSize,kernelSize));
        gridP.setSize(kernelSize*40,kernelSize*40);
        grids =new JTextField[kernelSize][kernelSize];
        for(int i=0;i<kernelSize;++i){
            for(int j=0;j<kernelSize;++j){
                grids[i][j] =new JTextField("0");
                grids[i][j].addKeyListener(new KeyInput(i,j));
                gridP.add(grids[i][j]);
            }
        }
    }
    
    private void setKernel(){
        divisor =Double.parseDouble(divT.getText());
        if(divisor==0.0){
            JOptionPane.showMessageDialog(frame,"Divisor number can not be 0 !!!","Warning!!!",JOptionPane.WARNING_MESSAGE);
            divT.setText(Double.toString(1.0));
            throw new IllegalArgumentException();
        }
        kernel =new double[kernelSize][kernelSize];
        for(int i=0;i<kernelSize;++i){
            for(int j=0;j<kernelSize;++j){
                kernel[i][j] =Double.parseDouble(grids[i][j].getText());
            }
        }
    }
    
    private class KeyInput extends KeyAdapter{
        private int x;
        private int y;
        public KeyInput(int i,int j){
            x =i;
            y =j;
        }
        
        @Override
        public void keyPressed(KeyEvent e) {
            int pos =grids[x][y].getCaretPosition();
            int length =grids[x][y].getText().length();
            if(e.getKeyCode() == KeyEvent.VK_UP){
                if(0 <= (x-1)){
                    grids[x-1][y].requestFocusInWindow();
                } 
            }
            else if(e.getKeyCode() == KeyEvent.VK_DOWN){
                if((x+1)<grids.length){
                    grids[x+1][y].requestFocusInWindow();
                }
            }
            else if(e.getKeyCode() == KeyEvent.VK_LEFT && pos ==0){
                if(0 <=(y-1)){
                    grids[x][y-1].requestFocusInWindow();
                }
            }
            else if(e.getKeyCode() == KeyEvent.VK_RIGHT && pos ==length){
                if((y+1)<grids.length){
                    grids[x][y+1].requestFocusInWindow();
                }
            }
            else if(e.getKeyCode() ==KeyEvent.VK_ENTER){
                if((y+1)<grids.length){
                    grids[x][y+1].requestFocusInWindow();
                }
                else if((x+1)<grids.length){
                    grids[x+1][0].requestFocusInWindow();
                }
            }
        } 
    }

    private class KernelInput implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            int size =Integer.parseInt(sizeT.getText());
                
            if(size< 3){
                JOptionPane.showMessageDialog(frame,"Kernel size can not be smaller than 3","Warning!!!",JOptionPane.WARNING_MESSAGE);
                sizeT.setText(Integer.toString(kernelSize));
            }
                
            else if(size%2 !=1){
                JOptionPane.showMessageDialog(frame,"Kernel size must be an odd number","Warning!!!",JOptionPane.WARNING_MESSAGE);
                sizeT.setText(Integer.toString(kernelSize));
            }
                
            else{
                kernelSize =size; 
                setGridPanel();
               
                if(kernelSize <= 9){
                    frame.setSize(600,200+gridP.getHeight());
                }
               else{
                    frame.setSize(180+gridP.getWidth(),200+gridP.getHeight());
                }
                divT.setLocation(100,130+gridP.getHeight()/2);
                divLabel.setLocation(30,130+gridP.getHeight()/2);
                frame.revalidate();
                frame.repaint();
            }
        }
    }
}

