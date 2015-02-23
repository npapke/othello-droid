/*
 
  $Id: Util.java,v 1.1 2002/10/30 19:59:32 npapke Exp $
  
  eOthello, an implementation of the Othello board game using
  a JSP front end with an EJB backend.

  Copyright 2001 Norbert Papke (npapke@acm.org). All Rights Reserved.

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  
*/


package ca.provenpath.othello.game;

/**
 *
 * @author  Norbert Papke
 * @version $Revision: 1.1 $
 */
public class Util
{
    public static final int EMPTY       = 0;
    public static final int WHITE       = 1;
    public static final int BLACK       = 2;
    public static final int VALID_WHITE = 3;
    public static final int VALID_BLACK = 4;
    
    public static final int BEGINNER     = 0;
    public static final int INTERMEDIATE = 1;
    public static final int EXPERT       = 2;
    
    public static final int DIM          = 8;
    
    public static final Cell EMPTY_CELL = new Cell( EMPTY );
    public static final Cell WHITE_CELL = new Cell( WHITE );
    public static final Cell BLACK_CELL = new Cell( BLACK );
    public static final Cell VALID_WHITE_CELL = new Cell( VALID_WHITE );
    public static final Cell VALID_BLACK_CELL = new Cell( VALID_BLACK );
    
}

