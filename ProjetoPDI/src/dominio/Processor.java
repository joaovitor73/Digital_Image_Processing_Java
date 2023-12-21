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
		ImageReader.imWrite(im,"C:\\Users\\joaov\\PDI\\Result\\"+ "_1_Imagem_original.png");
		
		//Reduzindo o tamanho da imagem
		im = ImaJ._imResize(im);
		ImageReader.imWrite(im,"C:\\Users\\joaov\\PDI\\Result\\"+ "_2_Imagem_reduzida.png");

		
		//Tranformando imagem RGB em CMYK
		int [][][] imCMYK = ImaJ.rgb2cmyk(im);
		ImageReader.imWrite(imCMYK,"C:\\Users\\joaov\\PDI\\Result\\"+ "_3_imagem_CMYK.png");


		//Pegando apenas o canal magenta
		int[][] im_magenta = ImaJ.splitChannel(imCMYK, 1);
		ImageReader.imWrite(im_magenta,"C:\\Users\\joaov\\PDI\\Result\\"+ "_4_Canal_Magenta.png");
		
		int[][] im_amarela = ImaJ.splitChannel(imCMYK, 2);
		ImageReader.imWrite(im_amarela,"C:\\Users\\joaov\\PDI\\Result\\"+ "_4_Canal_Amarela.png");

		int[][] im_preto = ImaJ.splitChannel(imCMYK, 3);
		ImageReader.imWrite(im_preto,"C:\\Users\\joaov\\PDI\\Result\\"+ "_4_Canal_preto.png");

		int[][] im_ciano = ImaJ.splitChannel(imCMYK, 0);
		ImageReader.imWrite(im_ciano,"C:\\Users\\joaov\\PDI\\Result\\"+ "_4_Canal_Ciano.png");


		//Binarizando a imagem
		boolean[][] tampas = ImaJ.im2bw(im_magenta, 100, true);
		ImageReader.imWrite(tampas,"C:\\Users\\joaov\\PDI\\Result\\"+"_5_Mascara_tampas_canal_magenta.png");
		
		//Removendo buracos das tampas com dilatação imagem com tampas
		tampas = ImaJ.bwDilate(tampas, 7);
		ImageReader.imWrite(tampas,"C:\\Users\\joaov\\PDI\\Result\\"+"_6_Tampa_dilatada.png");
		
		//Removendo  com erosão ruidos da imagem das tampas
		tampas = ImaJ.bwErode(tampas, 13);
		ImageReader.imWrite(tampas,"C:\\Users\\joaov\\PDI\\Result\\"+"_7_Tampa_erodida.png");
		
		//Binarizando o celular e a folha
		boolean[][] mask = ImaJ.im2bw_inv(im_magenta);
		ImageReader.imWrite(mask,"C:\\Users\\joaov\\PDI\\Result\\"+"_8-Macara_celular_folha_canal_magenta.png");
		
		//Dilatação para fechar buracos do celular e plantas
		mask = ImaJ.bwDilate(mask, 15);
		ImageReader.imWrite(mask,"C:\\Users\\joaov\\PDI\\Result\\"+"_9_Dilatacao_celular_folha.png");
		
		//Erosão para remover ruidos da imagem
		mask = ImaJ.bwErode(mask, 17);
		ImageReader.imWrite(mask,"C:\\Users\\joaov\\PDI\\Result\\"+"_10_Erosao_celular_folha.png");
	
		//boolean[][] folha = ImaJ.im2bw(im_amarela,25);
		//tampas = ImaJ.bwDilate(tampas, 9);
		//Separar tampa de folha
		//folha = ImaJ.sub(folha, tampas);
		//folha = ImaJ.bwErode(folha, 3);
		//ImageReader.imWrite(folha,"C:\\Users\\joaov\\PDI\\Result\\"+"_11_folha.png");
		
		ArrayList<Properties> tampas_bw = ImaJ.regionProps(tampas);
		ArrayList<Properties> celular_folha_bw = ImaJ.regionProps(mask);
		ArrayList<Properties> folha_bw;
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
				ImageReader.imWrite(im2, file.getPath().split("\\.")[0] + "_" + cont + "tampa.png");
				
				list.add(new Entity(tam.area, 0, "Vermelho", file.getPath().split("\\.")[0] +  "_" + cont + ".png", "Tampa"));
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
				imCMYK = ImaJ.rgb2cmyk(im2);
				im_amarela = ImaJ.splitChannel(imCMYK, 2);
				boolean[][] folha = ImaJ.im2bw(im_amarela,25, true);
				folha_bw = ImaJ.regionProps(folha);
				//ImageReader.imWrite(folha,"C:\\Users\\joaov\\PDI\\Result\\"+"_"+cont+"_folha.png");
				 
				// Aplicando máscara na imagem original
				for(int x = 0; x < im2.length; x++) {
					for(int y = 0; y < im2[0].length; y++) {
						//Se é pixel de fundo
						if(!cf.image[x][y]) {
							im2[x][y] = new int[]{0,0,0};
						}
					}
				}
				if(folha_bw.get(0).area > (media*0.01)) {
					list.add(new Entity(cf.area, folha_bw.get(0).area, "Verde", file.getPath().split("\\.")[0] +"_" + cont + ".png", "folha"));	
					ImageReader.imWrite(im2, file.getPath().split("\\.")[0] +"_" + cont + "folha.png");
				}else {
					list.add(new Entity(cf.area, folha_bw.get(0).area, "Preto", file.getPath().split("\\.")[0] + "_" + cont + ".png", "Celular"));
					ImageReader.imWrite(im2, file.getPath().split("\\.")[0] +"_" + cont + "celular.png");
				}
		
				cont++;
			}
			
		}	
		return list;
		
	}
}