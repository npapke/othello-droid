/*
 *  Copyright (c) 2015 Norbert Papke <npapke@acm.org>
 *
 *  This file is part of Othello.
 *
 *  Othello is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Othello is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Othello.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.provenpath.othello.game;

/**
 * A Strategy that simply optimizes the number of own pieces.
 * Created by npapke on 4/19/15.
 */
public class StrategyFactory
{
    public static Strategy getObject( String type )
    {
        switch (type.toLowerCase())
        {
            case "adaptive":
                return new AdaptiveStrategy();

            case "greedy":
                return new GreedyStrategy();

            case "static":
            default:
                return new StaticStrategy();
        }
    }
}
