package ch.epfl.cs211;

// Switch workspace ???

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

public class ImageProcessing extends PApplet {
	static String imgName = "board1.jpg"; // 800*600 img

	static int[][] GaussianBlurKernel = { { 9, 12, 9 }, { 12, 15, 12 },
			{ 9, 12, 9 } };
	static int[][] BoxBlurKernel = { { 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1 } };

	PImage img = loadImage(imgName);
	int i = 0;

	float hueMinColor = 108, hueMaxColor = 135, brightMinColor = 20,
			brightMaxColor = 145, satMinColor = 40, satMaxColor = 255,
			sobelThreshold = 0.2f;

	public void setup() {
		size(1200, 300);
	}

	public void draw() {
		PGraphics render = createGraphics(2400, 600);
		render.beginDraw();
		render.background(0);
		PImage imgResult1 = convolutionNormalized(img, GaussianBlurKernel);
		PImage imgResult2 = hueThresholding(imgResult1, hueMinColor,
				hueMaxColor);
		PImage imgResult3 = brightnessThresholding(imgResult2, brightMinColor,
				brightMaxColor);
		PImage imgResult4 = saturationThresholding(imgResult3, satMinColor,
				satMaxColor);
		PImage imgResult5 = sobel(imgResult4, sobelThreshold);
		int[] accumulator = hough(imgResult5);
		ArrayList<PVector> linesCoordinates = getCoordinates(imgResult5,
				accumulator, 4);
		ArrayList<PVector> intersect = getIntersections(linesCoordinates);
		PGraphics graph = drawLines(linesCoordinates, img);
		render.image(drawIntersection(intersect, graph), 0, 0);
		render.image(drawAccumulator(imgResult5, accumulator), 800, 0);
		render.image(imgResult5, 1600, 0);
		render.endDraw();
		render.resize(1200, 300);
		image(render, 0, 0);
	}

	public void keyPressed() {
		if (key == '1') {
			imgName = "board1.jpg";
			hueMinColor = 108;
			hueMaxColor = 135;
			brightMinColor = 20;
			brightMaxColor = 145;
			satMinColor = 40;
			satMaxColor = 255;
			sobelThreshold = 0.2f;
		} else if (key == '2') {
			imgName = "board2.jpg";
			hueMinColor = 111;
			hueMaxColor = 138;
			brightMinColor = 37;
			brightMaxColor = 142;
			satMinColor = 60;
			satMaxColor = 255;
			sobelThreshold = 0.1f;
		} else if (key == '3') {
			imgName = "board3.jpg";
			hueMinColor = 100;
			hueMaxColor = 130;
			brightMinColor = 20;
			brightMaxColor = 145;
			satMinColor = 40;
			satMaxColor = 255;
			sobelThreshold = 0.2f;
		} else if (key == '4') {
			imgName = "board4.jpg";
			hueMinColor = 90;
			hueMaxColor = 145;
			brightMinColor = 20;
			brightMaxColor = 145;
			satMinColor = 40;
			satMaxColor = 255;
			sobelThreshold = 0.2f;
		}
		img = loadImage(imgName);
	}

	private ArrayList<PVector> getIntersections(List<PVector> lines) {
		ArrayList<PVector> intersections = new ArrayList<PVector>();
		for (int i = 0; i < lines.size() - 1; i++) {
			PVector line1 = lines.get(i);
			for (int j = i + 1; j < lines.size(); j++) {
				PVector line2 = lines.get(j);
				float r1 = line1.x, r2 = line2.x, p1 = line1.y, p2 = line2.y;
				float d = cos(p2) * sin(p1) - cos(p1) * sin(p2);
				// compute the intersection and add it to 'intersections'
				int x = (int) ((r2 * sin(p1) - r1 * sin(p2)) / d);
				int y = (int) ((-r2 * cos(p1) + r1 * cos(p2)) / d);
				intersections.add(new PVector(x, y));
			}
		}
		return intersections;
	}

	/**
	 * Performs a thresholding operation on the given image, with the specified
	 * threshold.
	 * 
	 * @param img
	 *            - the image to be thresholded <b>WARNING :</b> will be
	 *            modified !
	 * @param threshold
	 *            - between 0 and 255
	 * 
	 * @return a black and white image, result of the thresholding operation.
	 */
	public PImage thresholding(PImage img, int threshold) {
		PImage result = createImage(img.width, img.height, ALPHA);
		img.filter(GRAY);
		img.loadPixels();
		result.loadPixels();
		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = (brightness(img.pixels[i]) < threshold) ? color(0)
					: color(255);
		} // 0 : White and 255: Black
		result.updatePixels();
		return result;
	}

	/**
	 * Performs a hue thresholding operation on the given image, with the
	 * specified min and max hue (between 0 and 255)
	 * 
	 * @param img
	 *            - the image to be hue thresholded
	 * @param colorMin
	 *            - minimum hue to be kept
	 * @param colorMax
	 *            - maximum hue to be kept
	 * 
	 * @return a black and white image, where all the pixels from <tt>img</tt>
	 *         whose hue ⋲ [<tt>colorMin</tt>, <tt>colorMax</tt>] are painted
	 *         in white, and the others painted in black.
	 */
	public PImage hueThresholding(PImage img, float colorMin, float colorMax) {
		PImage result = createImage(img.width, img.height, RGB);
		result.loadPixels();
		for (int i = 0; i < img.width * img.height; ++i) {
			if (colorMin <= hue(img.pixels[i])
					&& hue(img.pixels[i]) <= colorMax) {
				result.pixels[i] = img.pixels[i];
			} else {
				result.pixels[i] = color(255);
			}
		}
		result.updatePixels();
		return result;
	}

	public PImage saturationThresholding(PImage img, float saturationMin,
			float saturationMax) {
		PImage result = createImage(img.width, img.height, RGB);
		result.loadPixels();
		for (int i = 0; i < img.width * img.height; ++i) {
			if (saturationMin <= saturation(img.pixels[i])
					&& saturation(img.pixels[i]) <= saturationMax) {
				result.pixels[i] = img.pixels[i];
			} else {
				result.pixels[i] = color(255);
			}
		}
		result.updatePixels();
		return result;
	}

	public PImage brightnessThresholding(PImage img, float brightnessMin,
			float brightnessMax) {
		PImage result = createImage(img.width, img.height, RGB);
		result.loadPixels();
		for (int i = 0; i < img.width * img.height; ++i) {
			if (brightnessMin <= brightness(img.pixels[i])
					&& brightness(img.pixels[i]) <= brightnessMax) {
				result.pixels[i] = img.pixels[i];
			} else {
				result.pixels[i] = color(255);
			}
		}
		result.updatePixels();
		return result;
	}

	/**
	 * Performs a simple normalized convolution operation on the given image.
	 * 
	 * @param img
	 *            - the image to be convoluted
	 * @param Kernel
	 *            - the Kernel to convolute the image with
	 * 
	 * @return a new <tt>PImage</tt>, result of the normalized convolution of
	 *         the image with the kernel passed as parameters.
	 */
	public PImage convolutionNormalized(PImage img, int[][] Kernel) {
		int normalizingWeight = sumWeight(Kernel);
		return convolutionWithWeight(img, Kernel, normalizingWeight);
	}

	private int sumWeight(int[][] Kernel) {
		int sum = 0;
		for (int i = 0; i < Kernel.length; i++) {
			for (int j = 0; j < Kernel[i].length; j++)
				sum += Kernel[i][j];
		}
		return sum;
	}

	/**
	 * Performs a simple normalized convolution operation on the given image,
	 * using the specified weight.
	 * 
	 * @param img
	 *            - the image to be convoluted
	 * @param Kernel
	 *            - the Kernel to convolute the image with
	 * @param weight
	 *            - the divisor of all summed intensities
	 * 
	 * @return a new <tt>PImage</tt>, result of the convolution of the image
	 *         with the kernel passed as parameters. <br>
	 *         <b>Warning :</b> The borders of the image may seem darken.
	 */
	public PImage convolutionWithWeight(PImage img, int[][] Kernel, int weight) {
		PImage result = createImage(img.width, img.height, RGB);

		for (int x = Kernel.length / 2; x < img.width - Kernel.length / 2; x++) {
			for (int y = Kernel.length / 2; y < img.height - Kernel.length / 2; y++) {
				int reds = 0;
				int greens = 0;
				int blues = 0;

				int rasterPosition = x + y * img.width;

				for (int i = -Kernel.length / 2; i <= Kernel.length / 2; i++) {
					for (int j = -Kernel.length / 2; j <= Kernel.length / 2; j++) {
						reds += red(img.pixels[rasterPosition + i * img.width
								+ j])
								* Kernel[i + Kernel.length / 2][j
										+ Kernel.length / 2];
						blues += blue(img.pixels[rasterPosition + i * img.width
								+ j])
								* Kernel[i + Kernel.length / 2][j
										+ Kernel.length / 2];
						greens += green(img.pixels[rasterPosition + i
								* img.width + j])
								* Kernel[i + Kernel.length / 2][j
										+ Kernel.length / 2];
					}
				}

				result.pixels[rasterPosition] = color(reds / weight, greens
						/ weight, blues / weight);
			}
		}
		result.updatePixels();
		return result;
	}

	public PImage sobel(PImage img, double threshold) {

		int[][] hKernel = { { 0, 1, 0 }, { 0, 0, 0 }, { 0, -1, 0 } };
		int[][] vKernel = { { 0, 0, 0 }, { 1, 0, -1 }, { 0, 0, 0 } };

		float weight = 1.f; // Here : doesn't do anything (CAN be changed !)
		img.filter(GRAY);

		PImage result = createImage(img.width, img.height, ALPHA);
		result.loadPixels();
		for (int i = 0; i < img.width * img.height; i++) { // Clear the image
			result.pixels[i] = color(0);
		}

		double max = 0;
		double[] buffer = new double[img.width * img.height];

		// Does the double convolution :
		for (int x = 0; x < img.width; x++) {
			for (int y = 0; y < img.height; y++) {
				int sum_h = 0;
				int sum_v = 0;
				for (int i = 0; i < hKernel.length; i++) {
					for (int j = 0; j < hKernel[0].length; j++) {
						int clampedX = (x + i - 1 < 0) ? 0
								: ((x + i - 1 > img.width - 1) ? img.width - 1
										: x + i - 1);
						int clampedY = (y + j - 1 < 0) ? 0
								: ((y + j - 1 > img.height - 1) ? img.height - 1
										: y + j - 1);
						sum_h += brightness(img.pixels[clampedX + img.width
								* clampedY])
								* hKernel[i][j];
						sum_v += brightness(img.pixels[clampedX + img.width
								* clampedY])
								* vKernel[i][j];
					}
				}
				sum_h /= weight;
				sum_v /= weight;
				double sum = Math.sqrt(pow(sum_h, 2) + pow(sum_v, 2)); // euclidian
																		// distance
				buffer[x + img.width * y] = sum;
				max = (sum > max) ? sum : max;
			}
		}

		for (int y = 2; y < img.height - 2; y++) {
			for (int x = 2; x < img.width - 2; x++) {
				if (buffer[y * img.width + x] > (int) (max * threshold)) {
					result.pixels[y * img.width + x] = color(255);
				} else {
					result.pixels[y * img.width + x] = color(0);
				}
			}
		}

		result.updatePixels(); // Always call loadPixels() --> DO STUFF -->
								// updatePixels() --> return.
		return result;
	}

	public int[] hough(PImage edgeImg) {
		float discretizationStepsPhi = 0.06f;
		float discretizationStepsR = 2.5f;

		// dimensions of the accumulator
		int phiDim = (int) (Math.PI / discretizationStepsPhi);
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

		// our accumulator (with a 1 pix margin around)
		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

		// Fill the accumulator: on edge points (ie, white pixels of the edge
		// image), store all possible (r, phi) pairs
		// describing lines going through the point.

		for (int y = 0; y < edgeImg.height; y++) {
			for (int x = 0; x < edgeImg.width; x++) {
				// Are we on an edge? --> Pixel is white (or NOT black)
				if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {

					// ...determine here all the lines (r, phi) passing through
					// pixel (x,y), convert (r,phi) to coordinates
					// in the accumulator, and increment accordingly the
					// accumulator.

					for (int phi = 0; phi < phiDim; phi++) {
						float realPhi = phi * discretizationStepsPhi;
						double r = x * Math.cos(realPhi) + y
								* Math.sin(realPhi);
						int rAcc = (int) ((r / discretizationStepsR) + 0.5 * (rDim - 1));
						accumulator[(phi + 1) * (rDim + 2) + rAcc] += 1;
					}
				}
			}
		}
		return accumulator;
	}

	ArrayList<PVector> getCoordinates(PImage edgeImg, int[] accumulator,
			int nLines) {
		ArrayList<PVector> result = new ArrayList<>();
		float discretizationStepsPhi = 0.06f;
		float discretizationStepsR = 2.5f;
		int phiDim = (int) (Math.PI / discretizationStepsPhi);
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

		// take most voted line !!
		ArrayList<Integer> bestCandidates = new ArrayList<Integer>();

		int minVotes = 200;

		// size of the region we search for a local maximum
		int neighbourhood = 10;
		// only search around lines with more that this amount of votes
		// (to be adapted to your image)
		for (int accR = 0; accR < rDim; accR++) {
			for (int accPhi = 0; accPhi < phiDim; accPhi++) {
				// compute current index in the accumulator
				int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
				if (accumulator[idx] > minVotes) {
					boolean bestCandidate = true;
					// iterate over the neighbourhood
					for (int dPhi = -neighbourhood / 2; dPhi < neighbourhood / 2 + 1; dPhi++) {
						// check we are not outside the image
						if (accPhi + dPhi < 0 || accPhi + dPhi >= phiDim)
							continue;
						for (int dR = -neighbourhood / 2; dR < neighbourhood / 2 + 1; dR++) {
							// check we are not outside the image
							if (accR + dR < 0 || accR + dR >= rDim)
								continue;
							int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2)
									+ accR + dR + 1;
							if (accumulator[idx] < accumulator[neighbourIdx]) {
								// the current idx is not a local maximum!
								bestCandidate = false;
								break;
							}
						}
						if (!bestCandidate)
							break;
					}
					if (bestCandidate) {
						// the current idx *is* a local maximum
						bestCandidates.add(idx);
					}
				}
			}
		}
		Collections.sort(bestCandidates, new HoughComparator(accumulator));

		// Step 3 : return <r,phi> arrayList
		for (int idx = 0; idx < min(nLines, bestCandidates.size()); idx++) {
			// first, compute back the (r, phi) polar coordinates:
			int accPhi = (int) (bestCandidates.get(idx) / (rDim + 2)) - 1;
			int accR = bestCandidates.get(idx) - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
			float phi = accPhi * discretizationStepsPhi;
			result.add(new PVector(r, phi));
		}
		return result;
	}

	PImage drawAccumulator(PImage edgeImg, int[] accumulator) {
		float discretizationStepsPhi = 0.06f;
		float discretizationStepsR = 2.5f;
		int phiDim = (int) (Math.PI / discretizationStepsPhi);
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

		PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);
		houghImg.loadPixels();
		for (int i = 0; i < accumulator.length; i++) {
			houghImg.pixels[i] = color(min(255, accumulator[i]));
		}
		houghImg.updatePixels();
		houghImg.resize(edgeImg.width, edgeImg.height);
		return houghImg;
	}

	private PGraphics drawLines(ArrayList<PVector> takenLine, PImage baseImg) {
		PGraphics lines = createGraphics(baseImg.width, baseImg.height);
		lines.beginDraw();
		lines.background(baseImg);
		lines.stroke(204, 102, 0);
		for (int idx = 0; idx < takenLine.size(); idx++) {
			// Cartesian equation of a line: y = ax + b
			// in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
			// => y = 0 : x = r / cos(phi)
			// => x = 0 : y = r / sin(phi)
			// compute the intersection of this line with the 4 borders of
			// the image
			float r = takenLine.get(idx).x;
			float phi = takenLine.get(idx).y;
			int x0 = 0;
			int y0 = (int) (r / sin(phi));
			int x1 = (int) (r / cos(phi));
			int y1 = 0;
			int x2 = width;
			int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
			int y3 = width;
			int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));
			// y0 = min(y0, height);
			// x1 = min(x1, width);
			// y2 = min(y0, height);
			// x3 = min (x3, width);
			// Finally, plot the lines
			if (y0 > 0) {
				if (x1 > 0) {
					lines.line(x0, y0, x1, y1);
				} else if (y2 > 0) {
					lines.line(x0, y0, x2, y2);
				} else {
					lines.line(x0, y0, x3, y3);
				}
			} else {
				if (x1 > 0) {
					if (y2 > 0) {
						lines.line(x1, y1, x2, y2);
					} else {
						lines.line(x1, y1, x3, y3);
					}
				} else {
					lines.line(x2, y2, x3, y3);
				}
			}
		}
		lines.endDraw();
		return lines;
	}

	PGraphics drawIntersection(ArrayList<PVector> interCoords, PImage baseImage) {
		PGraphics intersection = createGraphics(baseImage.width,
				baseImage.height);
		intersection.beginDraw();
		intersection.background(baseImage);
		intersection.stroke(204, 102, 0);
		intersection.fill(255, 128, 0);
		for (PVector v : interCoords) {
			intersection.ellipse(v.x, v.y, 10, 10);
		}
		intersection.endDraw();
		return intersection;
	}

}