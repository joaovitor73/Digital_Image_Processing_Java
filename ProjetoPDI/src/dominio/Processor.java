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
		
		int[][][] im = ImageReader.imRead(file.getPath());
		im = ImaJ._imResize(im);
		
		int [][][] blur = ImaJ.imGaussian(im, 11);
		
		//im=blur;
	
		//int[][] im_red = ImaJ.splitChannel(im, 0);
		//imageShow.imShow(im_red,"VERMELHO");
		//int[][] im_green = ImaJ.splitChannel(im, 1);
		//imageShow.imShow(im_green,"VERDE");
		//int[][] im_blue = ImaJ.splitChannel(im, 2);
		//imageShow.imShow(im_blue,"AZUL");
		
		
		//int[][][] im_blur = ImaJ.imGaussian(im, 5);
		int [][][] imCMYK = ImaJ.rgb2cmyk(im);

		//int[][] im_ciano = ImaJ.splitChannel(imCMYK , 0);
		//imageShow.imShow(im_ciano, file.getPath(), "Ciano");

		int[][] im_magenta = ImaJ.splitChannel(imCMYK, 1);
		//imageShow.imShow(im_magenta, file.getPath(),"Magente");

		//int[][] im_amarelo = ImaJ.splitChannel(imCMYK, 2);
		//imageShow.imShow(im_amarelo, file.getPath(),"Amarelo");
		
		//int [][] im_preto = ImaJ.splitChannel(imCMYK, 3);;
		//imageShow.imShow(im_preto, file.getPath(),"Peto");
		
		boolean[][] tampas = ImaJ.im2bw(im_magenta, 100);
		
		
		//tampas = ImaJ.imGaussian(tampas,5);
		//Preencher buracos da tampa
		tampas = ImaJ.bwDilate(tampas, 7);
		//Remover ruidos da imagem
		tampas = ImaJ.bwErode(tampas, 13);
		//imageShow.imShow(tampas, "Tampa");
		
		boolean[][] mask = ImaJ.im2bw_inv(im_magenta);
		//imageShow.imShow(mask,file.getPath());
		mask = ImaJ.bwDilate(mask, 15);
		//Remover ruidos da imagem
		mask = ImaJ.bwErode(mask, 17);
		//Recuperar caule
		//mask = ImaJ.bwDilate(mask,3);
		//imageShow.imShow(mask, "Mascara");
		
		//boolean[][] saida = ImaJ.sum(tampas, mask);
		//imageShow.imShow(saida,"Saida");
		

	
		ArrayList<Properties> sementes = ImaJ.regionProps(tampas);
		ArrayList<Properties> sementes2 = ImaJ.regionProps(mask);
		float media = 0;
		for(Properties s : sementes) {
			media += s.area;
		}
		media /= sementes.size();
		int cont = 0;
		for(Properties s : sementes) {
			if(Math.abs(media-s.area) > (media*0.01)) {
				int[][][] im2 = ImaJ.imCrop(im, s.boundingBox[0], s.boundingBox[1], 
						                        s.boundingBox[2], s.boundingBox[3]);
				 
				// Aplicando máscara na imagem original
				for(int x = 0; x < im2.length; x++) {
					for(int y = 0; y < im2[0].length; y++) {
						//Se é pixel de fundo
						if(!s.image[x][y]) {
							im2[x][y] = new int[]{0,0,0};
						}
					}
				}
				ImageReader.imWrite(im2, file.getPath().split("\\.")[0] + "_" + cont + ".png");
				
				list.add(new Entity(s.area, 1, file.getPath().split("\\.")[0] + "_" + cont + ".png", "grande"));
				cont++;
			}
		}
		
		for(Properties s : sementes2) {
			int[][][] im2 = ImaJ.imCrop(im, s.boundingBox[0], s.boundingBox[1], 
					                        s.boundingBox[2], s.boundingBox[3]);
			 
			// Aplicando máscara na imagem original
			for(int x = 0; x < im2.length; x++) {
				for(int y = 0; y < im2[0].length; y++) {
					//Se é pixel de fundo
					if(!s.image[x][y]) {
						im2[x][y] = new int[]{0,0,0};
					}
				}
			}
			ImageReader.imWrite(im2, file.getPath().split("\\.")[0] + "_" + cont + ".png");
			
			list.add(new Entity(s.area, 1, file.getPath().split("\\.")[0] + "_" + cont + ".png", "grande"));	
			cont++;
			
		}
		
		return list;
		
	}
}