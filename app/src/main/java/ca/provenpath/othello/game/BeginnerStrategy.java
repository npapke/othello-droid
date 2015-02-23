/*
 
  $Id: BeginnerStrategy.java,v 1.1 2002/10/30 19:59:31 npapke Exp $
  
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
public class BeginnerStrategy extends Strategy
{
    /** Creates new BeginnerStrategy */
    public BeginnerStrategy( Cell player )
    {
        super( player, 1 );
    }
    
    public int determineBoardValue( Board board ) throws OthelloException
    {
        return determineFinalScore( board ) / 100;  // magnitude non-terminal board values should be smaller
    }

}
