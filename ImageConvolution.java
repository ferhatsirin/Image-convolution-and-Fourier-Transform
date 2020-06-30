import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.round;
import vpt.Image;
import vpt.ByteImage;

public class ImageConvolution {
    /**
     * Calculate convolution operation and return new image
     * @param img
     * @param kernel 
     * @param divisor 
     * @return 
     */
    public static Image convolution(Image img,double[][] kernel,double divisor){
        int width =img.getXDim();
        int height =img.getYDim();   
        kernel =flipKernel(kernel);
        Image filtered = new ByteImage(width,height,1);
        int value;
        for(int j =0; j< height;++j){
            for(int i=0;i<width;++i){
                value =calculate(img,i,j,kernel,divisor);
                if(255 <value)
                    value =255;
                else if(value <0)
                    value =0;
                
                filtered.setXYByte(i, j, value);
            }
        }
        return filtered;
    }
        
    /**
     * Calculate the value for x,y position in the image by using the kernel
     * @param img
     * @param x
     * @param y
     * @param kernel
     * @param divisor
     * @return 
     */
    private static int calculate(Image img,int x,int y,double[][] kernel,double divisor){
       int startX =x-(kernel.length/2); int startY =y-(kernel.length/2);
       double sum =0.0;
       for(int j=0;  j<kernel.length && (j+startY) <img.getYDim();++j){
           for(int i=0; i<kernel.length && (i+startX) <img.getXDim();++i){
               if(0 <=(i+startX) && 0 <=(j+startY)){
                   sum =sum+kernel[j][i]*img.getXYByte(i+startX,j+startY);
               }
           }
       }
        return (int) round(sum/divisor);
    }

    private static double[][] flipKernel(double[][] kernel){
        int size =kernel.length;
        double[][] flipped = new double[size][size];
        
        for(int i=0;i<size;++i){
            for(int j=0;j<size;++j){
                flipped[i][j] =kernel[size-i-1][size-j-1];
            }
        }
        return flipped;
    }
  
    /**
     * Calculate the convolution theorem with fourier transform
     * @param img
     * @param kernel
     * @param divisor
     * @return 
     */
    public static Image convolutionWithFourier(Image img,double[][] kernel,double divisor){
        
        int sizeX =paddingSize(img.getYDim()+kernel.length-1);
        int sizeY =paddingSize(img.getXDim()+kernel[0].length-1);

        Complex[][] imgArr =prepareImage(img,sizeX,sizeY);
        fourierTransform2D(imgArr,1);
 
        Complex[][] kernelArr =prepareKernel(kernel,divisor,sizeX,sizeY);
        fourierTransform2D(kernelArr,1);
        
        for(int i=0;i<imgArr.length;++i){
            for(int j=0;j<imgArr[i].length;++j){
                imgArr[i][j] =imgArr[i][j].times(kernelArr[i][j]);
            }
        }
        
        fourierTransform2D(imgArr,-1); 
        
        combineImage(imgArr);
               
        int value;
        for(int i=0;i<img.getXDim();++i){
            for(int j =0;j<img.getYDim();++j){
                value = (int) round(imgArr[j][i].real/(imgArr.length*imgArr[0].length));
                if(255<value )
                    value =255;
                else if(value <0)
                    value =0;
                img.setXYByte(i, j, value);
            }
        }
   
        return img;
    }
    
    /**
     * Calculate fourier transform for 2 dimenision
     * Makes changes in place.
     * @param arr 
     * @param dir 1 for forward, -1 for inverse transform
     */
    
   private static void fourierTransform2D(Complex[][] arr,int dir){
       
        /* Transform the columns */
        Complex[] temp =new Complex[arr.length];
        for (int i=0; i<arr[0].length;++i){
            for (int j=0;j<arr.length;++j) {
                temp[j] =arr[j][i];
            }
            fastFourierTransform(temp,dir);
        }
   
        /* Transform the rows */
        for (int i=0;i<arr.length;++i) {
            fastFourierTransform(arr[i],dir);
        }
    }
   
   /**
    * Calculate fourier transform for 1 dimension
    * Makes changes in place
    * @param x
    * @param dir 1 for forward, -1 for inverse transform
    */
   private static void fastFourierTransform(Complex[] x,int dir) {

        Complex temp =new Complex();
        /* bit reverse permutation operation to sort array 
         so that even numbers will be primary indices, odd number will be secondary */
        int shift = 1 + Integer.numberOfLeadingZeros(x.length);
        for (int k = 0; k < x.length; k++) {
            int j = Integer.reverse(k) >>> shift;
            if (j > k) {
                temp.set(x[j]);
                x[j].set(x[k]);
                x[k].set(temp);
            }
        }
       
        /* fast fourier transform */ 
        Complex exp =new Complex();
        double angle;        
        for (int L = 2; L <= x.length; L = L+L) {
            for (int k = 0; k < L/2; k++) {
                angle = -2.0 * dir*k * Math.PI / L;
                exp.set(Math.cos(angle), Math.sin(angle));
                for (int j = 0; j < x.length/L; j++) {
                    Complex tao = exp.times(x[j*L + k + L/2]);
                    x[j*L + k + L/2].set(x[j*L + k].minus(tao)); 
                    x[j*L + k].set(x[j*L + k].plus(tao)); 
                }
            }
        }
    }

   /**
    * Return the smallest number that is bigger than or equal to size
    * Return number is power of 2
    * @param size
    * @return 
    */
    private static int paddingSize(int size){
        while((size & (size-1)) !=0){
            ++size;
        }
        return size;
    }
    /**
     * Prepare complex array for image with zero padded
     * @param img
     * @param sizeX for x dimension
     * @param sizeY for y dimension
     * @return 
     */
    private static Complex[][] prepareImage(Image img,int sizeX,int sizeY){
        Complex[][] out=new Complex[sizeX][sizeY];
        
        for(int i=0;i<img.getXDim();++i){
            for(int j=0;j<img.getYDim();++j){
                out[j][i] =new Complex(img.getXYByte(i,j),0.0);
            }
        }
        for(int i=img.getXDim();i < sizeY;++i){
            for(int j=0;j<sizeX;++j){
                out[j][i] =new Complex();
            }
        }
        for(int i=0;i<img.getXDim();++i){
            for(int j=img.getYDim();j<sizeX;++j){
                out[j][i] =new Complex();
            }
        }
        
        return out;
    }
    /**
     * Prepare complex array that kernel is at the center and zero pad the array
     * @param kernel
     * @param sizeX for x dimension
     * @param sizeY for y dimension
     * @return 
     */
    private static Complex[][] prepareKernel(double[][] kernel,double divisor,int sizeX,int sizeY){
        Complex[][] out =new Complex[sizeX][sizeY];
        
        for(int i=0;i<sizeX;++i){
            for(int j=0;j<sizeY;++j){
                out[i][j] =new Complex();
            }
        }
         
        int startX =(int) round((sizeX-kernel.length)/2.0);
        int startY =(int) round((sizeY-kernel.length)/2.0);
        for(int i=startX;i< kernel.length+startX;++i){
            for(int j=startY;j<kernel.length+startY;++j){
                out[i][j] =new Complex(kernel[i-startX][j-startY]/divisor,0.0);
            }
        }
        
        return out;
    }
    /**
     * After inverse fourier transform, image is disjoined. 
     * To combine image, apply swap operation between rows an columns
     * @param imgArr 
     */
    private static void combineImage(Complex[][] imgArr){

        /* swap for columns */
        Complex[] temp =new Complex[(int)floor(imgArr[0].length/2.0)];
        for(int i=0;i<imgArr.length;++i){
            for(int j=(int) (floor(imgArr[0].length/2.0));j<imgArr[0].length;++j){
                temp[j-(int) (floor(imgArr[0].length/2.0))] =imgArr[i][j];
            }
            for(int j=0;j<floor(imgArr[0].length/2.0);++j){
                imgArr[i][(int)ceil(imgArr[0].length/2.0)+j] =imgArr[i][j];
            }
            for(int j=0;j<floor(imgArr[0].length/2.0);++j){
                imgArr[i][j] =temp[j];
            }
        }

        /* swap for rows */
        temp =new Complex[(int)floor(imgArr.length/2.0)];        
        for(int i=0;i<imgArr[0].length;++i){
            for(int j=(int) (floor(imgArr.length/2.0));j<imgArr.length;++j){
                temp[j-(int) (floor(imgArr.length/2.0))] =imgArr[j][i];
            }
            for(int j=0;j<floor(imgArr.length/2.0);++j){
                imgArr[(int)ceil(imgArr.length/2.0)+j][i] =imgArr[j][i];
            }
            for(int j=0;j<floor(imgArr.length/2.0);++j){
                imgArr[j][i] =temp[j];
            }
        }
    
    }
    
    private static class Complex{
        private double real;
        private double imag;
        
        public Complex(double r,double i){
            real =r;
            imag =i;
        }
        public Complex(){
            this(0,0);
        }
        
        public void setReal(double r){
            real =r;
        }
        public void setImag(double i){
            imag =i;
        }
        
        public void set(Complex c){
            real =c.real;
            imag =c.imag;
        }
        public void set(double r,double i){
            real =r;
            imag =i;
        }
        
        public Complex plus(Complex num){
            double r =real+num.real;
            double i =imag+num.imag;
            
            return new Complex(r,i);
        }
        
        public Complex minus(Complex num){
            double r =real-num.real;
            double i=imag-num.imag;
            
            return new Complex(r,i);
        }
        
        public Complex times(Complex num){
            double r=real*num.real-imag*num.imag;
            double i =real*num.imag+imag*num.real;
            
            return new Complex(r,i);
        }
    }
}

