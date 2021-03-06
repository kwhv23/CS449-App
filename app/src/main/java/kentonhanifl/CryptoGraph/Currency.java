package kentonhanifl.CryptoGraph;

import java.util.Comparator;

public class Currency
{
    public String MarketName;
    public float Last;
    public float PrevDay;
    boolean favorite;

    Currency()
    {
        favorite = false;
        MarketName="";
        Last = 0;
        PrevDay = 0;
    }

    @Override
    public String toString()
    {
        return this.getName();
    }


    //Just want to hide the BTC- from the name since default prices are always shown in BTC.
    //Otherwise, we can display the full market name
    public String getName()
    {
        StringBuffer nameBuffer = new StringBuffer(MarketName);
        if(MarketName.startsWith("BTC-"))
        {
            return nameBuffer.substring(4, nameBuffer.length());
        }
        else return MarketName;
    }

    //unit testable
    public Float getChange()
    {
        return(Last/PrevDay - 1)*(float)100.00;
    }


    //Overridden for checking if currencies are apart of the Currencies ArrayList in main
    //I define equivalency as "The market name is the same"
    //unit testable
    @Override
    public boolean equals(Object o) {
        Currency currency = (Currency) o;
        if (this == o) return true; //Are they the same object?
        if (o == null || getClass() != o.getClass()) return false; //Is o null or not even of this class?

        if(currency.MarketName.equals(MarketName))
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    //Overriden because people on StackOverflow said equals() and hashCode() are commonly overridden together.
    //This is a pruned down version of the automatically generated override of hashCode()
    @Override
    public int hashCode() {
        int result = MarketName != null ? MarketName.hashCode() : 0;
        return result;
    }


}

/*
----------------------------
COMPARATORS
----------------------------
*/

class CurrencyNameCompare implements Comparator<Currency>
{
    @Override
    public int compare(Currency lhs, Currency rhs) {

            //Normal sorting
            if (    !lhs.getName().startsWith("USDT-")
                    &&
                    !rhs.getName().startsWith("USDT-"))
            {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }

            //Compare BTC to USDT market (BTC comes first)
            else if (   !lhs.getName().startsWith("USDT-")
                        &&
                        rhs.getName().startsWith("USDT-"))
            {
                return -1;
            }

            //Compare USDT to BTC market (BTC comes first)
            else if (   lhs.getName().startsWith("USDT-")
                        &&
                        !rhs.getName().startsWith("USDT-"))
            {
                return 1;
            }

            //Compare USDT to USDT (normal sorting)
            else if (   lhs.getName().startsWith("USDT-")
                        &&
                        rhs.getName().startsWith("USDT-"))
            {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        return 0;
    }
}

class BackwardsCurrencyNameCompare implements Comparator<Currency>
{
    @Override
    public int compare(Currency lhs, Currency rhs) {

        //Normal sorting
        if (    !lhs.getName().startsWith("USDT-")
                &&
                !rhs.getName().startsWith("USDT-"))
        {
            return rhs.getName().compareToIgnoreCase(lhs.getName());
        }

        //Compare BTC to USDT market (BTC comes first)
        else if (   !lhs.getName().startsWith("USDT-")
                &&
                rhs.getName().startsWith("USDT-"))
        {
            return -1;
        }

        //Compare USDT to BTC market (BTC comes first)
        else if (   lhs.getName().startsWith("USDT-")
                &&
                !rhs.getName().startsWith("USDT-"))
        {
            return 1;
        }

        //Compare USDT to USDT (normal sorting)
        else if (   lhs.getName().startsWith("USDT-")
                &&
                rhs.getName().startsWith("USDT-"))
        {
            return rhs.getName().compareToIgnoreCase(lhs.getName());
        }
        return 0;
    }
}

//----------

class CurrencyPriceCompare implements Comparator<Currency>
{
    @Override
    public int compare(Currency lhs, Currency rhs) {

        //Normal sorting
        if (    !lhs.getName().startsWith("USDT-")
                &&
                !rhs.getName().startsWith("USDT-"))
        {
            return Float.compare(rhs.Last,lhs.Last);
        }

        //Compare BTC to USDT market (BTC comes first)
        else if (   !lhs.getName().startsWith("USDT-")
                    &&
                    rhs.getName().startsWith("USDT-"))
        {
            return -1;
        }

        //Compare USDT to BTC market (BTC comes first)
        else if (   lhs.getName().startsWith("USDT-")
                    &&
                    !rhs.getName().startsWith("USDT-"))
        {
            return 1;
        }

        //Compare USDT to USDT (normal sorting)
        else if (   lhs.getName().startsWith("USDT-")
                    &&
                    rhs.getName().startsWith("USDT-"))
        {
            return Float.compare(rhs.Last,lhs.Last);
        }


        return 0;
    }
}

class BackwardsCurrencyPriceCompare implements Comparator<Currency>
{
    @Override
    public int compare(Currency lhs, Currency rhs) {

        //Normal sorting
        if (    !lhs.getName().startsWith("USDT-")
                &&
                !rhs.getName().startsWith("USDT-"))
        {
            return Float.compare(lhs.Last,rhs.Last);
        }

        //Compare BTC to USDT market (BTC comes first)
        else if (   !lhs.getName().startsWith("USDT-")
                    &&
                    rhs.getName().startsWith("USDT-"))
        {
            return -1;
        }

        //Compare USDT to BTC market (BTC comes first)
        else if (   lhs.getName().startsWith("USDT-")
                    &&
                    !rhs.getName().startsWith("USDT-"))
        {
            return 1;
        }

        //Compare USDT to USDT (normal sorting)
        else if (   lhs.getName().startsWith("USDT-")
                    &&
                    rhs.getName().startsWith("USDT-"))
        {
            return Float.compare(lhs.Last,rhs.Last);
        }
        return 0;
    }
}

//----------

class CurrencyFavoriteCompare implements Comparator<Currency>
{
    @Override
    public int compare(Currency lhs, Currency rhs) {

        //If both are favorited, do a CurrencyNameCompare.
        if (lhs.favorite && rhs.favorite)
        {
            CurrencyNameCompare Compare = new CurrencyNameCompare();
            return Compare.compare(lhs,rhs);
        }
        //If neither are favorited, do a CurrencyNameCompare.
        else if (!lhs.favorite && !rhs.favorite)
        {
            CurrencyNameCompare Compare = new CurrencyNameCompare();
            return Compare.compare(lhs,rhs);
        }

        //If one is favorited and one isn't, compare by that.
        else if (lhs.favorite && !rhs.favorite)
        {
            return -1;
        }

        else if (!lhs.favorite && rhs.favorite)
        {
            return 1;
        }

        return 0;
    }
}

//----------

class CurrencyChangeCompare implements Comparator<Currency>
{
    @Override
    public int compare(Currency lhs, Currency rhs) {

        //Normal sorting
        if (    !lhs.getName().startsWith("USDT-")
                &&
                !rhs.getName().startsWith("USDT-"))
        {
            return Float.compare(rhs.getChange(),lhs.getChange());
        }

        //Compare BTC to USDT market (BTC comes first)
        else if (   !lhs.getName().startsWith("USDT-")
                &&
                rhs.getName().startsWith("USDT-"))
        {
            return -1;
        }

        //Compare USDT to BTC market (BTC comes first)
        else if (   lhs.getName().startsWith("USDT-")
                &&
                !rhs.getName().startsWith("USDT-"))
        {
            return 1;
        }

        //Compare USDT to USDT (normal sorting)
        else if (   lhs.getName().startsWith("USDT-")
                &&
                rhs.getName().startsWith("USDT-"))
        {
            return Float.compare(rhs.getChange(),lhs.getChange());
        }
        return 0;
    }
}

class BackwardsCurrencyChangeCompare implements Comparator<Currency>
{
    @Override
    public int compare(Currency lhs, Currency rhs) {

        //Normal sorting
        if (    !lhs.getName().startsWith("USDT-")
                &&
                !rhs.getName().startsWith("USDT-"))
        {
            return Float.compare(lhs.getChange(),rhs.getChange());
        }

        //Compare BTC to USDT market (BTC comes first)
        else if (   !lhs.getName().startsWith("USDT-")
                &&
                rhs.getName().startsWith("USDT-"))
        {
            return -1;
        }

        //Compare USDT to BTC market (BTC comes first)
        else if (   lhs.getName().startsWith("USDT-")
                &&
                !rhs.getName().startsWith("USDT-"))
        {
            return 1;
        }

        //Compare USDT to USDT (normal sorting)
        else if (   lhs.getName().startsWith("USDT-")
                &&
                rhs.getName().startsWith("USDT-"))
        {
            return Float.compare(lhs.getChange(),rhs.getChange());
        }
        return 0;
    }
}


class CurrencyChartData
{
    public float O;
    public float C;
    public float H;
    public float L;
    public float V;
    public float BV;
    public String T;

    public String getDateTime()
    {
        if (T.equals(""))
        {
            return "";
        }
        String returnedDateTime;
        StringBuffer buffer = new StringBuffer(T);
        int t = buffer.indexOf("T");
        buffer.replace(t,t+1," ");
        int colon = buffer.lastIndexOf(":");
        buffer.delete(colon,buffer.length());
        returnedDateTime = buffer.toString();
        return returnedDateTime;
    }

    CurrencyChartData() {
        O = C = H = L = V = BV = 0;
        T="";
    }

}