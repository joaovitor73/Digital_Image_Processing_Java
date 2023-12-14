package dominio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dominio.ImaJ.ImaJ;
import dominio.ImaJ.Properties;
import persistencia.ImageReader;
import visao.ImageShow;

public class Processor {

	public List<Entity> process(File file) {
		ImageShow imageShow = new ImageShow();
		
		ArrayList<Entity> list = new ArrayList<>();
		
		//Lendo a imagem
		int[][][] im = ImageReader.imRead(file.getPath());
		//ImageReader.imWrite(im,"C:\\Users\\joaov\\PDI\\Result\\"+ "_1_Imagem_original.png");
		
		//Reduzindo o tamanho da imagem
		im = ImaJ._imResize(im);
		//ImageReader.imWrite(im,"C:\\Users\\joaov\\PDI\\Result\\"+ "_2_Imagem_reduzida.png");

		
		//Tranformando imagem RGB em CMYK
		int [][][] imCMYK = ImaJ.rgb2cmyk(im);
		//ImageReader.imWrite(imCMYK,"C:\\Users\\joaov\\PDI\\Result\\"+ "_3_imagem_CMYK.png");


		//Pegando apenas o canal magenta
		int[][] im_magenta = ImaJ.splitChannel(imCMYK, 1);
		//ImageReader.imWrite(im_magenta,"C:\\Users\\joaov\\PDI\\Result\\"+ "_4_Canal_Magenta.png");

		//Binarizando a imagem
		boolean[][] tampas = ImaJ.im2bw(im_magenta, 100);
		//ImageReader.imWrite(tampas,"C:\\Users\\joaov\\PDI\\Result\\"+"_5_Mascara_tampas_canal_magenta.png");
		
		//Removendo buracos das tampas com dilatação imagem com tampas
		tampas = ImaJ.bwDilate(tampas, 7);
		//ImageReader.imWrite(tampas,"C:\\Users\\joaov\\PDI\\Result\\"+"_6_Tampa_dilatada.png");
		
		//Removendo  com erosão ruidos da imagem das tampas
		tampas = ImaJ.bwErode(tampas, 13);
		//ImageReader.imWrite(tampas,"C:\\Users\\joaov\\PDI\\Result\\"+"_7_Tampa_erodida.png");
		
		//Binarizando o celular e a folha
		boolean[][] mask = ImaJ.im2bw_inv(im_magenta);
		//ImageReader.imWrite(mask,"C:\\Users\\joaov\\PDI\\Result\\"+"_8-Macara_celular_folha_canal_magenta.png");
		
		//Dilatação para fechar buracos do celular e plantas
		mask = ImaJ.bwDilate(mask, 15);
		//ImageReader.imWrite(mask,"C:\\Users\\joaov\\PDI\\Result\\"+"_9_Dilatacao_celular_folha.png");
		
		//Erosão para remover ruidos da imagem
		mask = ImaJ.bwErode(mask, 17);
		//ImageReader.imWrite(mask,"C:\\Users\\joaov\\PDI\\Result\\"+"_10_Erosao_celular_folha.png");
	
		ArrayList<Properties> tampas_bw = ImaJ.regionProps(tampas);
		ArrayList<Properties> celular_folha_bw = ImaJ.regionProps(mask);
		float media = 0;
		for(Properties tam : tampas_bw) {
			media += tam.area;
		}
		media /= tampas_bw.size();
		int cont = 0;
		for(Properties tam :tampas_bw) {
			if((tam.area > (media*0.01))) {
				int[][][] im2 = ImaJ.imCrop(im, tam.boundingBox[0], tam.boundingBox[1], 
						                        tam.boundingBox[2], tam.boundingBox[3]);
				 
				// Aplicando máscara na imagem original
				for(int x = 0; x < im2.length; x++) {
					for(int y = 0; y < im2[0].length; y++) {
						//Se é pixel de fundo
						if(!tam.image[x][y]) {
							im2[x][y] = new int[]{0,0,0};
						}
					}
				}
				ImageReader.imWrite(im2, "C:\\Users\\joaov\\PDI\\Result\\"+ "_" + cont + ".png");
				
				list.add(new Entity(tam.area, 1, "C:\\Users\\joaov\\PDI\\Result\\"+  "_" + cont + ".png", "Tampa"));
				cont++;
			}
		}
		
		for(Properties cf : celular_folha_bw) {
			media += cf.area;
		}
		media /= celular_folha_bw.size();
		
		for(Properties cf : celular_folha_bw) {
			if((cf.area > (media*0.01))) {
				int[][][] im2 = ImaJ.imCrop(im, cf.boundingBox[0],cf.boundingBox[1], 
						                        cf.boundingBox[2], cf.boundingBox[3]);
				 
				// Aplicando máscara na imagem original
				for(int x = 0; x < im2.length; x++) {
					for(int y = 0; y < im2[0].length; y++) {
						//Se é pixel de fundo
						if(!cf.image[x][y]) {
							im2[x][y] = new int[]{0,0,0};
						}
					}
				}
				ImageReader.imWrite(im2, "C:\\Users\\joaov\\PDI\\Result\\"+ "_" + cont + ".png");
				
				list.add(new Entity(cf.area, 1, "C:\\Users\\joaov\\PDI\\Result\\" + "_" + cont + ".png", "grande"));	
				cont++;
			}
			
		}	
		return list;
		
	}
}