/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    Colors.java
 *    Copyright (C) 1999 Malcolm Ware
 *
 */

package weka.gui.treevisualizer;

import javax.swing.*;
import java.awt.*;

/**
 * This class maintains a list that contains all the colornames from the 
 * dotty standard and what color (in RGB) they represent
 *
 * @author Malcolm Ware (mfw4@cs.waikato.ac.nz)
 * @version $Revision: 1.3 $
 */
public class Colors {
  
  /** The array with all the colors input */
  NamedColor[] m_cols = {
		       new NamedColor("snow",255, 250, 250),
		       new NamedColor("ghostwhite",248, 248, 255),
		       new NamedColor("whitesmoke",245, 245, 245),
		       new NamedColor("gainsboro",220, 220, 220),
		       new NamedColor("floralwhite",255, 250, 240),
		       new NamedColor("oldlace",253, 245, 230),
		       new NamedColor("linen",250, 240, 230),
		       new NamedColor("antiquewhite",250, 235, 215),
		       new NamedColor("papayawhip",255, 239, 213),
		       new NamedColor("blanchedalmond",255, 235, 205),
		       new NamedColor("bisque",255, 228, 196),
		       new NamedColor("peachpuff",255, 218, 185),
		       new NamedColor("navajowhite",255, 222, 173),
		       new NamedColor("moccasin",255, 228, 181),
		       new NamedColor("cornsilk",255, 248, 220),
		       new NamedColor("ivory",255, 255, 240),
		       new NamedColor("lemonchiffon",255, 250, 205),
		       new NamedColor("seashell",255, 245, 238),
		       new NamedColor("honeydew",240, 255, 240),
		       new NamedColor("mintcream",245, 255, 250),
		       new NamedColor("azure",240, 255, 255),
		       new NamedColor("aliceblue",240, 248, 255),
		       new NamedColor("lavender",230, 230, 250),
		       new NamedColor("lavenderblush",255, 240, 245),
		       new NamedColor("mistyrose",255, 228, 225),
		       new NamedColor("white",255, 255, 255),
		       new NamedColor("black",  0,   0,   0),
		       new NamedColor("darkslategray", 47,  79,  79),
		       new NamedColor("dimgray",105, 105, 105),
		       new NamedColor("slategray",112, 128, 144),
		       new NamedColor("lightslategray",119, 136, 153),
		       new NamedColor("gray",190, 190, 190),
		       new NamedColor("lightgray",211, 211, 211),
		       new NamedColor("midnightblue", 25,  25, 112),
		       new NamedColor("navy",  0,   0, 128),
		       new NamedColor("cornflowerblue",100, 149, 237),
		       new NamedColor("darkslateblue", 72,  61, 139),
		       new NamedColor("slateblue",106,  90, 205),
		       new NamedColor("mediumslateblue",123, 104, 238),
		       new NamedColor("lightslateblue",132, 112, 255),
		       new NamedColor("mediumblue",  0,   0, 205),
		       new NamedColor("royalblue", 65, 105, 225),
		       new NamedColor("blue",  0,   0, 255),
		       new NamedColor("dodgerblue", 30, 144, 255),
		       new NamedColor("deepskyblue",  0, 191, 255),
		       new NamedColor("skyblue",135, 206, 235),
		       new NamedColor("lightskyblue",135, 206, 250),
		       new NamedColor("steelblue", 70, 130, 180),
		       new NamedColor("lightsteelblue",176, 196, 222),
		       new NamedColor("lightblue",173, 216, 230),
		       new NamedColor("powderblue",176, 224, 230),
		       new NamedColor("paleturquoise",175, 238, 238),
		       new NamedColor("darkturquoise",  0, 206, 209),
		       new NamedColor("mediumturquoise", 72, 209, 204),
		       new NamedColor("turquoise", 64, 224, 208),
		       new NamedColor("cyan",  0, 255, 255),
		       new NamedColor("lightcyan",224, 255, 255),
		       new NamedColor("cadetblue", 95, 158, 160),
		       new NamedColor("mediumaquamarine",102, 205, 170),
		       new NamedColor("aquamarine",127, 255, 212),
		       new NamedColor("darkgreen",  0, 100,   0),
		       new NamedColor("darkolivegreen", 85, 107,  47),
		       new NamedColor("darkseagreen",143, 188, 143),
		       new NamedColor("seagreen", 46, 139,  87),
		       new NamedColor("mediumseagreen", 60, 179, 113),
		       new NamedColor("lightseagreen", 32, 178, 170),
		       new NamedColor("palegreen",152, 251, 152),
		       new NamedColor("springgreen",  0, 255, 127),
		       new NamedColor("lawngreen",124, 252,   0),
		       new NamedColor("green",  0, 255,   0),
		       new NamedColor("chartreuse",127, 255,   0),
		       new NamedColor("mediumspringgreen",  0, 250, 154),
		       new NamedColor("greenyellow",173, 255,  47),
		       new NamedColor("limegreen", 50, 205,  50),
		       new NamedColor("yellowgreen",154, 205,  50),
		       new NamedColor("forestgreen", 34, 139,  34),
		       new NamedColor("olivedrab",107, 142,  35),
		       new NamedColor("darkkhaki",189, 183, 107),
		       new NamedColor("khaki",240, 230, 140),
		       new NamedColor("palegoldenrod",238, 232, 170),
		       new NamedColor("lightgoldenrodyellow",250, 250, 210),
		       new NamedColor("lightyellow",255, 255, 224),
		       new NamedColor("yellow",255, 255,   0),
		       new NamedColor("gold",255, 215,   0),
		       new NamedColor("lightgoldenrod",238, 221, 130),
		       new NamedColor("goldenrod",218, 165,  32),
		       new NamedColor("darkgoldenrod",184, 134,  11),
		       new NamedColor("rosybrown",188, 143, 143),
		       new NamedColor("indianred",205,  92,  92),
		       new NamedColor("saddlebrown",139,  69,  19),
		       new NamedColor("sienna",160,  82,  45),
		       new NamedColor("peru",205, 133,  63),
		       new NamedColor("burlywood",222, 184, 135),
		       new NamedColor("beige",245, 245, 220),
		       new NamedColor("wheat",245, 222, 179),
		       new NamedColor("sandybrown",244, 164,  96),
		       new NamedColor("tan",210, 180, 140),
		       new NamedColor("chocolate",210, 105,  30),
		       new NamedColor("firebrick",178,  34,  34),
		       new NamedColor("brown",165,  42,  42),
		       new NamedColor("darksalmon",233, 150, 122),
		       new NamedColor("salmon",250, 128, 114),
		       new NamedColor("lightsalmon",255, 160, 122),
		       new NamedColor("orange",255, 165,   0),
		       new NamedColor("darkorange",255, 140,   0),
		       new NamedColor("coral",255, 127,  80),
		       new NamedColor("lightcoral",240, 128, 128),
		       new NamedColor("tomato",255,  99,  71),
		       new NamedColor("orangered",255,  69,   0),
		       new NamedColor("red",255,   0,   0),
		       new NamedColor("hotpink",255, 105, 180),
		       new NamedColor("deeppink",255,  20, 147),
		       new NamedColor("pink",255, 192, 203),
		       new NamedColor("lightpink",255, 182, 193),
		       new NamedColor("palevioletred",219, 112, 147),
		       new NamedColor("maroon",176,  48,  96),
		       new NamedColor("mediumvioletred",199,  21, 133),
		       new NamedColor("violetred",208,  32, 144),
		       new NamedColor("magenta",255,   0, 255),
		       new NamedColor("violet",238, 130, 238),
		       new NamedColor("plum",221, 160, 221),
		       new NamedColor("orchid",218, 112, 214),
		       new NamedColor("mediumorchid",186,  85, 211),
		       new NamedColor("darkorchid",153,  50, 204),
		       new NamedColor("darkviolet",148,   0, 211),
		       new NamedColor("blueviolet",138,  43, 226),
		       new NamedColor("purple",160,  32, 240),
		       new NamedColor("mediumpurple",147, 112, 219),
		       new NamedColor("thistle",216, 191, 216),
		       new NamedColor("snow1",255, 250, 250),
		       new NamedColor("snow2",238, 233, 233),
		       new NamedColor("snow3",205, 201, 201),
		       new NamedColor("snow4",139, 137, 137),
		       new NamedColor("seashell1",255, 245, 238),
		       new NamedColor("seashell2",238, 229, 222),
		       new NamedColor("seashell3",205, 197, 191),
		       new NamedColor("seashell4",139, 134, 130),
		       new NamedColor("antiquewhite1",255, 239, 219),
		       new NamedColor("antiquewhite2",238, 223, 204),
		       new NamedColor("antiquewhite3",205, 192, 176),
		       new NamedColor("antiquewhite4",139, 131, 120),
		       new NamedColor("bisque1",255, 228, 196),
		       new NamedColor("bisque2",238, 213, 183),
		       new NamedColor("bisque3",205, 183, 158),
		       new NamedColor("bisque4",139, 125, 107),
		       new NamedColor("peachpuff1",255, 218, 185),
		       new NamedColor("peachpuff2",238, 203, 173),
		       new NamedColor("peachpuff3",205, 175, 149),
		       new NamedColor("peachpuff4",139, 119, 101),
		       new NamedColor("navajowhite1",255, 222, 173),
		       new NamedColor("navajowhite2",238, 207, 161),
		       new NamedColor("navajowhite3",205, 179, 139),
		       new NamedColor("navajowhite4",139, 121,	 94),
		       new NamedColor("lemonchiffon1",255, 250, 205),
		       new NamedColor("lemonchiffon2",238, 233, 191),
		       new NamedColor("lemonchiffon3",205, 201, 165),
		       new NamedColor("lemonchiffon4",139, 137, 112),
		       new NamedColor("cornsilk1",255, 248, 220),
		       new NamedColor("cornsilk2",238, 232, 205),
		       new NamedColor("cornsilk3",205, 200, 177),
		       new NamedColor("cornsilk4",139, 136, 120),
		       new NamedColor("ivory1",255, 255, 240),
		       new NamedColor("ivory2",238, 238, 224),
		       new NamedColor("ivory3",205, 205, 193),
		       new NamedColor("ivory4",139, 139, 131),
		       new NamedColor("honeydew1",240, 255, 240),
		       new NamedColor("honeydew2",224, 238, 224),
		       new NamedColor("honeydew3",193, 205, 193),
		       new NamedColor("honeydew4",131, 139, 131),
		       new NamedColor("lavenderblush1",255, 240, 245),
		       new NamedColor("lavenderblush2",238, 224, 229),
		       new NamedColor("lavenderblush3",205, 193, 197),
		       new NamedColor("lavenderblush4",139, 131, 134),
		       new NamedColor("mistyrose1",255, 228, 225),
		       new NamedColor("mistyrose2",238, 213, 210),
		       new NamedColor("mistyrose3",205, 183, 181),
		       new NamedColor("mistyrose4",139, 125, 123),
		       new NamedColor("azure1",240, 255, 255),
		       new NamedColor("azure2",224, 238, 238),
		       new NamedColor("azure3",193, 205, 205),
		       new NamedColor("azure4",131, 139, 139),
		       new NamedColor("slateblue1",131, 111, 255),
		       new NamedColor("slateblue2",122, 103, 238),
		       new NamedColor("slateblue3",105,  89, 205),
		       new NamedColor("slateblue4", 71,  60, 139),
		       new NamedColor("royalblue1", 72, 118, 255),
		       new NamedColor("royalblue2", 67, 110, 238),
		       new NamedColor("royalblue3", 58,  95, 205),
		       new NamedColor("royalblue4", 39,  64, 139),
		       new NamedColor("blue1",  0,   0, 255),
		       new NamedColor("blue2",  0,   0, 238),
		       new NamedColor("blue3",  0,   0, 205),
		       new NamedColor("blue4",  0,   0, 139),
		       new NamedColor("dodgerblue1", 30, 144, 255),
		       new NamedColor("dodgerblue2", 28, 134, 238),
		       new NamedColor("dodgerblue3", 24, 116, 205),
		       new NamedColor("dodgerblue4", 16,  78, 139),
		       new NamedColor("steelblue1", 99, 184, 255),
		       new NamedColor("steelblue2", 92, 172, 238),
		       new NamedColor("steelblue3", 79, 148, 205),
		       new NamedColor("steelblue4", 54, 100, 139),
		       new NamedColor("deepskyblue1",  0, 191, 255),
		       new NamedColor("deepskyblue2",  0, 178, 238),
		       new NamedColor("deepskyblue3",  0, 154, 205),
		       new NamedColor("deepskyblue4",  0, 104, 139),
		       new NamedColor("skyblue1",135, 206, 255),
		       new NamedColor("skyblue2",126, 192, 238),
		       new NamedColor("skyblue3",108, 166, 205),
		       new NamedColor("skyblue4", 74, 112, 139),
		       new NamedColor("lightskyblue1",176, 226, 255),
		       new NamedColor("lightskyblue2",164, 211, 238),
		       new NamedColor("lightskyblue3",141, 182, 205),
		       new NamedColor("lightskyblue4", 96, 123, 139),
		       new NamedColor("slategray1",198, 226, 255),
		       new NamedColor("slategray2",185, 211, 238),
		       new NamedColor("slategray3",159, 182, 205),
		       new NamedColor("slategray4",108, 123, 139),
		       new NamedColor("lightsteelblue1",202, 225, 255),
		       new NamedColor("lightsteelblue2",188, 210, 238),
		       new NamedColor("lightsteelblue3",162, 181, 205),
		       new NamedColor("lightsteelblue4",110, 123, 139),
		       new NamedColor("lightblue1",191, 239, 255),
		       new NamedColor("lightblue2",178, 223, 238),
		       new NamedColor("lightblue3",154, 192, 205),
		       new NamedColor("lightblue4",104, 131, 139),
		       new NamedColor("lightcyan1",224, 255, 255),
		       new NamedColor("lightcyan2",209, 238, 238),
		       new NamedColor("lightcyan3",180, 205, 205),
		       new NamedColor("lightcyan4",122, 139, 139),
		       new NamedColor("paleturquoise1",187, 255, 255),
		       new NamedColor("paleturquoise2",174, 238, 238),
		       new NamedColor("paleturquoise3",150, 205, 205),
		       new NamedColor("paleturquoise4",102, 139, 139),
		       new NamedColor("cadetblue1",152, 245, 255),
		       new NamedColor("cadetblue2",142, 229, 238),
		       new NamedColor("cadetblue3",122, 197, 205),
		       new NamedColor("cadetblue4", 83, 134, 139),
		       new NamedColor("turquoise1",  0, 245, 255),
		       new NamedColor("turquoise2",  0, 229, 238),
		       new NamedColor("turquoise3",  0, 197, 205),
		       new NamedColor("turquoise4",  0, 134, 139),
		       new NamedColor("cyan1",  0, 255, 255),
		       new NamedColor("cyan2",  0, 238, 238),
		       new NamedColor("cyan3",  0, 205, 205),
		       new NamedColor("cyan4",  0, 139, 139),
		       new NamedColor("darkslategray1",151, 255, 255),
		       new NamedColor("darkslategray2",141, 238, 238),
		       new NamedColor("darkslategray3",121, 205, 205),
		       new NamedColor("darkslategray4", 82, 139, 139),
		       new NamedColor("aquamarine1",127, 255, 212),
		       new NamedColor("aquamarine2",118, 238, 198),
		       new NamedColor("aquamarine3",102, 205, 170),
		       new NamedColor("aquamarine4", 69, 139, 116),
		       new NamedColor("darkseagreen1",193, 255, 193),
		       new NamedColor("darkseagreen2",180, 238, 180),
		       new NamedColor("darkseagreen3",155, 205, 155),
		       new NamedColor("darkseagreen4",105, 139, 105),
		       new NamedColor("seagreen1", 84, 255, 159),
		       new NamedColor("seagreen2", 78, 238, 148),
		       new NamedColor("seagreen3", 67, 205, 128),
		       new NamedColor("seagreen4", 46, 139,	 87),
		       new NamedColor("palegreen1",154, 255, 154),
		       new NamedColor("palegreen2",144, 238, 144),
		       new NamedColor("palegreen3",124, 205, 124),
		       new NamedColor("palegreen4", 84, 139,	 84),
		       new NamedColor("springgreen1",  0, 255, 127),
		       new NamedColor("springgreen2",  0, 238, 118),
		       new NamedColor("springgreen3",  0, 205, 102),
		       new NamedColor("springgreen4",  0, 139,	 69),
		       new NamedColor("green1",  0, 255,	  0),
		       new NamedColor("green2",  0, 238,	  0),
		       new NamedColor("green3",  0, 205,	  0),
		       new NamedColor("green4",  0, 139,	  0),
		       new NamedColor("chartreuse1",127, 255,	  0),
		       new NamedColor("chartreuse2",118, 238,	  0),
		       new NamedColor("chartreuse3",102, 205,	  0),
		       new NamedColor("chartreuse4", 69, 139,	  0),
		       new NamedColor("olivedrab1",192, 255,	 62),
		       new NamedColor("olivedrab2",179, 238,	 58),
		       new NamedColor("olivedrab3",154, 205,	 50),
		       new NamedColor("olivedrab4",105, 139,	 34),
		       new NamedColor("darkolivegreen1",202, 255, 112),
		       new NamedColor("darkolivegreen2",188, 238, 104),
		       new NamedColor("darkolivegreen3",162, 205,	 90),
		       new NamedColor("darkolivegreen4",110, 139,	 61),
		       new NamedColor("khaki1",255, 246, 143),
		       new NamedColor("khaki2",238, 230, 133),
		       new NamedColor("khaki3",205, 198, 115),
		       new NamedColor("khaki4",139, 134,	 78),
		       new NamedColor("lightgoldenrod1",255, 236, 139),
		       new NamedColor("lightgoldenrod2",238, 220, 130),
		       new NamedColor("lightgoldenrod3",205, 190, 112),
		       new NamedColor("lightgoldenrod4",139, 129,	 76),
		       new NamedColor("lightyellow1",255, 255, 224),
		       new NamedColor("lightyellow2",238, 238, 209),
		       new NamedColor("lightyellow3",205, 205, 180),
		       new NamedColor("lightyellow4",139, 139, 122),
		       new NamedColor("yellow1",255, 255,	  0),
		       new NamedColor("yellow2",238, 238,	  0),
		       new NamedColor("yellow3",205, 205,	  0),
		       new NamedColor("yellow4",139, 139,	  0),
		       new NamedColor("gold1",255, 215,	  0),
		       new NamedColor("gold2",238, 201,	  0),
		       new NamedColor("gold3",205, 173,	  0),
		       new NamedColor("gold4",139, 117,	  0),
		       new NamedColor("goldenrod1",255, 193,	 37),
		       new NamedColor("goldenrod2",238, 180,	 34),
		       new NamedColor("goldenrod3",205, 155,	 29),
		       new NamedColor("goldenrod4",139, 105,	 20),
		       new NamedColor("darkgoldenrod1",255, 185,	 15),
		       new NamedColor("darkgoldenrod2",238, 173,	 14),
		       new NamedColor("darkgoldenrod3",205, 149,	 12),
		       new NamedColor("darkgoldenrod4",139, 101,	  8),
		       new NamedColor("rosybrown1",255, 193, 193),
		       new NamedColor("rosybrown2",238, 180, 180),
		       new NamedColor("rosybrown3",205, 155, 155),
		       new NamedColor("rosybrown4",139, 105, 105),
		       new NamedColor("indianred1",255, 106, 106),
		       new NamedColor("indianred2",238,  99,	 99),
		       new NamedColor("indianred3",205,  85,	 85),
		       new NamedColor("indianred4",139,  58,	 58),
		       new NamedColor("sienna1",255, 130,	 71),
		       new NamedColor("sienna2",238, 121,	 66),
		       new NamedColor("sienna3",205, 104,	 57),
		       new NamedColor("sienna4",139,  71,	 38),
		       new NamedColor("burlywood1",255, 211, 155),
		       new NamedColor("burlywood2",238, 197, 145),
		       new NamedColor("burlywood3",205, 170, 125),
		       new NamedColor("burlywood4",139, 115,	 85),
		       new NamedColor("wheat1",255, 231, 186),
		       new NamedColor("wheat2",238, 216, 174),
		       new NamedColor("wheat3",205, 186, 150),
		       new NamedColor("wheat4",139, 126, 102),
		       new NamedColor("tan1",255, 165,	 79),
		       new NamedColor("tan2",238, 154,	 73),
		       new NamedColor("tan3",205, 133,	 63),
		       new NamedColor("tan4",139,  90,	 43),
		       new NamedColor("chocolate1",255, 127,	 36),
		       new NamedColor("chocolate2",238, 118,	 33),
		       new NamedColor("chocolate3",205, 102,	 29),
		       new NamedColor("chocolate4",139,  69,	 19),
		       new NamedColor("firebrick1",255,  48,	 48),
		       new NamedColor("firebrick2",238,  44,	 44),
		       new NamedColor("firebrick3",205,  38,	 38),
		       new NamedColor("firebrick4",139,  26,	 26),
		       new NamedColor("brown1",255,  64,	 64),
		       new NamedColor("brown2",238,  59,	 59),
		       new NamedColor("brown3",205,  51,	 51),
		       new NamedColor("brown4",139,  35,	 35),
		       new NamedColor("salmon1",255, 140, 105),
		       new NamedColor("salmon2",238, 130,	 98),
		       new NamedColor("salmon3",205, 112,	 84),
		       new NamedColor("salmon4",139,  76,	 57),
		       new NamedColor("lightsalmon1",255, 160, 122),
		       new NamedColor("lightsalmon2",238, 149, 114),
		       new NamedColor("lightsalmon3",205, 129,	 98),
		       new NamedColor("lightsalmon4",139,  87,	 66),
		       new NamedColor("orange1",255, 165,	  0),
		       new NamedColor("orange2",238, 154,	  0),
		       new NamedColor("orange3",205, 133,	  0),
		       new NamedColor("orange4",139,  90,	  0),
		       new NamedColor("darkorange1",255, 127,	  0),
		       new NamedColor("darkorange2",238, 118,	  0),
		       new NamedColor("darkorange3",205, 102,	  0),
		       new NamedColor("darkorange4",139,  69,	  0),
		       new NamedColor("coral1",255, 114,	 86),
		       new NamedColor("coral2",238, 106,	 80),
		       new NamedColor("coral3",205,  91,	 69),
		       new NamedColor("coral4",139,  62,	 47),
		       new NamedColor("tomato1",255,  99,	 71),
		       new NamedColor("tomato2",238,  92,	 66),
		       new NamedColor("tomato3",205,  79,	 57),
		       new NamedColor("tomato4",139,  54,	 38),
		       new NamedColor("orangered1",255,  69,	  0),
		       new NamedColor("orangered2",238,  64,	  0),
		       new NamedColor("orangered3",205,  55,	  0),
		       new NamedColor("orangered4",139,  37,	  0),
		       new NamedColor("red1",255,   0,	  0),
		       new NamedColor("red2",238,   0,	  0),
		       new NamedColor("red3",205,   0,	  0),
		       new NamedColor("red4",139,   0,	  0),
		       new NamedColor("deeppink1",255,  20, 147),
		       new NamedColor("deeppink2",238,  18, 137),
		       new NamedColor("deeppink3",205,  16, 118),
		       new NamedColor("deeppink4",139,  10,	 80),
		       new NamedColor("hotpink1",255, 110, 180),
		       new NamedColor("hotpink2",238, 106, 167),
		       new NamedColor("hotpink3",205,  96, 144),
		       new NamedColor("hotpink4",139,  58,  98),
		       new NamedColor("pink1",255, 181, 197),
		       new NamedColor("pink2",238, 169, 184),
		       new NamedColor("pink3",205, 145, 158),
		       new NamedColor("pink4",139,  99, 108),
		       new NamedColor("lightpink1",255, 174, 185),
		       new NamedColor("lightpink2",238, 162, 173),
		       new NamedColor("lightpink3",205, 140, 149),
		       new NamedColor("lightpink4",139,  95, 101),
		       new NamedColor("palevioletred1",255, 130, 171),
		       new NamedColor("palevioletred2  ",238, 121, 159),
		       new NamedColor("palevioletred3",205, 104, 137),
		       new NamedColor("palevioletred4",139,  71,	 93),
		       new NamedColor("maroon1",255,  52, 179),
		       new NamedColor("maroon2",238,  48, 167),
		       new NamedColor("maroon3",205,  41, 144),
		       new NamedColor("maroon4",139,  28,	 98),
		       new NamedColor("violetred1",255,  62, 150),
		       new NamedColor("violetred2",238,  58, 140),
		       new NamedColor("violetred3",205,  50, 120),
		       new NamedColor("violetred4",139,  34,	 82),
		       new NamedColor("magenta1",255,   0, 255),
		       new NamedColor("magenta2",238,   0, 238),
		       new NamedColor("magenta3",205,   0, 205),
		       new NamedColor("magenta4",139,   0, 139),
		       new NamedColor("orchid1",255, 131, 250),
		       new NamedColor("orchid2",238, 122, 233),
		       new NamedColor("orchid3",205, 105, 201),
		       new NamedColor("orchid4",139,  71, 137),
		       new NamedColor("plum1",255, 187, 255),
		       new NamedColor("plum2",238, 174, 238),
		       new NamedColor("plum3",205, 150, 205),
		       new NamedColor("plum4",139, 102, 139),
		       new NamedColor("mediumorchid1",224, 102, 255),
		       new NamedColor("mediumorchid2",209,  95, 238),
		       new NamedColor("mediumorchid3",180,  82, 205),
		       new NamedColor("mediumorchid4",122,  55, 139),
		       new NamedColor("darkorchid1",191,  62, 255),
		       new NamedColor("darkorchid2",178,  58, 238),
		       new NamedColor("darkorchid3",154,  50, 205),
		       new NamedColor("darkorchid4",104,  34, 139),
		       new NamedColor("purple1",155,  48, 255),
		       new NamedColor("purple2",145,  44, 238),
		       new NamedColor("purple3",125,  38, 205),
		       new NamedColor("purple4", 85,  26, 139),
		       new NamedColor("mediumpurple1",171, 130, 255),
		       new NamedColor("mediumpurple2",159, 121, 238),
		       new NamedColor("mediumpurple3",137, 104, 205),
		       new NamedColor("mediumpurple4", 93,  71, 139),
		       new NamedColor("thistle1",255, 225, 255),
		       new NamedColor("thistle2",238, 210, 238),
		       new NamedColor("thistle3",205, 181, 205),
		       new NamedColor("thistle4",139, 123, 139),
		       new NamedColor("gray0",  0,   0,   0),
		       new NamedColor("gray1",  3,   3,   3),
		       new NamedColor("gray2",  5,   5,   5),
		       new NamedColor("gray3",  8,   8,   8),
		       new NamedColor("gray4", 10,  10,  10),
		       new NamedColor("gray5", 13,  13,  13),
		       new NamedColor("gray6", 15,  15,  15),
		       new NamedColor("gray7", 18,  18,  18),
		       new NamedColor("gray8", 20,  20,  20),
		       new NamedColor("gray9", 23,  23,  23),
		       new NamedColor("gray10", 26,  26,  26),
		       new NamedColor("gray11", 28,  28,  28),
		       new NamedColor("gray12", 31,  31,  31),
		       new NamedColor("gray13", 33,  33,  33),
		       new NamedColor("gray14", 36,  36,  36),
		       new NamedColor("gray15", 38,  38,  38),
		       new NamedColor("gray16", 41,  41,  41),
		       new NamedColor("gray17", 43,  43,  43),
		       new NamedColor("gray18", 46,  46,  46),
		       new NamedColor("gray19", 48,  48,  48),
		       new NamedColor("gray20", 51,  51,  51),
		       new NamedColor("gray21", 54,  54,  54),
		       new NamedColor("gray22", 56,  56,  56),
		       new NamedColor("gray23", 59,  59,  59),
		       new NamedColor("gray24", 61,  61,  61),
		       new NamedColor("gray25", 64,  64,  64),
		       new NamedColor("gray26", 66,  66,  66),
		       new NamedColor("gray27", 69,  69,  69),
		       new NamedColor("gray28", 71,  71,  71),
		       new NamedColor("gray29", 74,  74,  74),
		       new NamedColor("gray30", 77,  77,  77),
		       new NamedColor("gray31", 79,  79,  79),
		       new NamedColor("gray32", 82,  82,  82),
		       new NamedColor("gray33", 84,  84,  84),
		       new NamedColor("gray34", 87,  87,  87),
		       new NamedColor("gray35", 89,  89,  89),
		       new NamedColor("gray36", 92,  92,  92),
		       new NamedColor("gray37", 94,  94,  94),
		       new NamedColor("gray38", 97,  97,  97),
		       new NamedColor("gray39", 99,  99,  99),
		       new NamedColor("gray40",102, 102, 102),
		       new NamedColor("gray41",105, 105, 105),
		       new NamedColor("gray42",107, 107, 107),
		       new NamedColor("gray43",110, 110, 110),
		       new NamedColor("gray44",112, 112, 112),
		       new NamedColor("gray45",115, 115, 115),
		       new NamedColor("gray46",117, 117, 117),
		       new NamedColor("gray47",120, 120, 120),
		       new NamedColor("gray48",122, 122, 122),
		       new NamedColor("gray49",125, 125, 125),
		       new NamedColor("gray50",127, 127, 127),
		       new NamedColor("gray51",130, 130, 130),
		       new NamedColor("gray52",133, 133, 133),
		       new NamedColor("gray53",135, 135, 135),
		       new NamedColor("gray54",138, 138, 138),
		       new NamedColor("gray55",140, 140, 140),
		       new NamedColor("gray56",143, 143, 143),
		       new NamedColor("gray57",145, 145, 145),
		       new NamedColor("gray58",148, 148, 148),
		       new NamedColor("gray59",150, 150, 150),
		       new NamedColor("gray60",153, 153, 153),
		       new NamedColor("gray61",156, 156, 156),
		       new NamedColor("gray62",158, 158, 158),
		       new NamedColor("gray63",161, 161, 161),
		       new NamedColor("gray64",163, 163, 163),
		       new NamedColor("gray65",166, 166, 166),
		       new NamedColor("gray66",168, 168, 168),
		       new NamedColor("gray67",171, 171, 171),
		       new NamedColor("gray68",173, 173, 173),
		       new NamedColor("gray69",176, 176, 176),
		       new NamedColor("gray70",179, 179, 179),
		       new NamedColor("gray71",181, 181, 181),
		       new NamedColor("gray72",184, 184, 184),
		       new NamedColor("gray73",186, 186, 186),
		       new NamedColor("gray74",189, 189, 189),
		       new NamedColor("gray75",191, 191, 191),
		       new NamedColor("gray76",194, 194, 194),
		       new NamedColor("gray77",196, 196, 196),
		       new NamedColor("gray78",199, 199, 199),
		       new NamedColor("gray79",201, 201, 201),
		       new NamedColor("gray80",204, 204, 204),
		       new NamedColor("gray81",207, 207, 207),
		       new NamedColor("gray82",209, 209, 209),
		       new NamedColor("gray83",212, 212, 212),
		       new NamedColor("gray84",214, 214, 214),
		       new NamedColor("gray85",217, 217, 217),
		       new NamedColor("gray86",219, 219, 219),
		       new NamedColor("gray87",222, 222, 222),
		       new NamedColor("gray88",224, 224, 224),
		       new NamedColor("gray89",227, 227, 227),
		       new NamedColor("gray90",229, 229, 229),
		       new NamedColor("gray91",232, 232, 232),
		       new NamedColor("gray92",235, 235, 235),
		       new NamedColor("gray93",237, 237, 237),
		       new NamedColor("gray94",240, 240, 240),
		       new NamedColor("gray95",242, 242, 242),
		       new NamedColor("gray96",245, 245, 245),
		       new NamedColor("gray97",247, 247, 247),
		       new NamedColor("gray98",250, 250, 250),
		       new NamedColor("gray99",252, 252, 252),
		       new NamedColor("gray100",255, 255, 255),
		       new NamedColor("darkgray",169, 169, 169),
		       new NamedColor("darkblue",0  ,   0, 139),
		       new NamedColor("darkcyan",0  , 139, 139),
		       new NamedColor("darkmagenta",139,   0, 139),
		       new NamedColor("darkred",139,   0,   0),
		       new NamedColor("lightgreen",144, 238, 144),


  };
}
