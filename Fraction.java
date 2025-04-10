package thing.value;

import java.util.ArrayList;

import static tool.MathTool.*;

public class Fraction implements RealNumber<Fraction> {
    //constants:
    public static Fraction PI_26=new Fraction(8958937768937l,2851718461558l);
    //attributes:
    private long p=0;
    private long q=1;
    //getters:
    public int intValue() {
        return (int) (p/q);
    }
    public long longValue() {
        return p/q;
    }
    public float floatValue() {
        return (float)p/q;
    }
    public double doubleValue() {
        return (double)p/q;
    }
    public long denominator(){
        return q;
    }
    public long numerator(){
        return p;
    }
    ///constructors:
    public Fraction(){
    }
    public Fraction(long m, long n){//m是分子,n是分母
        constructFromFraction(m,n);
    }
    public Fraction(long m){//m是整数，即分子
        p=m;
    }
    public Fraction(double m){//m是分子
        constructFromDouble(m);
    }
    public Fraction(float m){//m是分子
        constructFromFloat(m);
    }
    public static Fraction DecimalToFraction(long integerPart,long decimalPart){
        Fraction result=new Fraction();
        result.constructFromDecimal(integerPart,decimalPart);
        return result;
    }
    /**
     *
     * @param x
     * @param errorLimit the E in oldFrac*(1+E)=newFrac for (newFrac>oldFrac) or oldFrac/(1+E)=new Frac for (newFrac<oldFrac)
     * @param maxDenominator the max denominator allowed to have for high precision
     * @return a new Fraction contain those values.
     */
    public static Fraction roundFractionRatioErrorIterate(double x,double errorLimit,long maxDenominator){
        long minErrorQ=1;
        long sign=(long)sign(x);
        x=abs(x);
        double minError=POS_INF;
        double error;
        double ceilError;
        double floorError;
        boolean isCeilMinError=false;
        double newP;
        for(long newQ=1;newQ<=maxDenominator;newQ++){
            newP=x*newQ;
            ceilError=longCeil(newP)/x/newQ;
            floorError=newQ*x/longFloor(newP);
            error=min(ceilError,floorError);
            if(error<minError){
                minErrorQ=newQ;
                isCeilMinError=ceilError<floorError;
                if(error<=errorLimit){
                    break;
                }
                minError=error;
            }
        }
        return new Fraction(sign*(isCeilMinError?longCeil(x*minErrorQ):longFloor(x*minErrorQ)),minErrorQ);
    }
    /**
     *
     * @param x
     * @param errorLimit
     * @param maxDenominator
     * @return
     * core idea: making the error=big/small.
     */
    public static Fraction roundFractionAbsErrorIterate(double x,double errorLimit,long maxDenominator){
        long minErrorQ=1;
        long sign=(long)sign(x);
        x=abs(x);
        double minError=POS_INF;
        double error;
        double newP;
        for(long newQ=1;newQ<=maxDenominator;newQ++){
            newP=longRound(x*newQ);
            error=abs(x-newP/newQ);
            if(error<minError){
                minErrorQ=newQ;
                if(error<=errorLimit){
                    break;
                }
                minError=error;
            }
        }
        return new Fraction(sign*longRound(x*minErrorQ),minErrorQ);
    }
    public static Fraction roundFractionContinuedFraction(double a, double errorLimit, long maxDenominator) {
        double sign = Math.signum(a);
        a = Math.abs(a);

        ArrayList<Integer> continuedFractionList = new ArrayList<>();

        // 递归构造连分数列表
        buildContinuedFraction(a, continuedFractionList, errorLimit, 0, 100); // 最多展开100项，避免死循环

        // 逐项逼近直到分母或误差超限
        long p0 = continuedFractionList.get(0), q0 = 1;
        long p1 = 1, q1 = 0;

        for (int i = 1; i < continuedFractionList.size(); i++) {
            long a_i = continuedFractionList.get(i);
            long p2, q2;
            try {
                p2 = Math.addExact(Math.multiplyExact(a_i, p0), p1);
                q2 = Math.addExact(Math.multiplyExact(a_i, q0), q1);
            } catch (ArithmeticException e) {
                break; // 溢出，停止
            }

            double approx = (double)p2 / q2;
            if (q2 > maxDenominator || Math.abs(approx - a * sign) <= errorLimit) {
                return new Fraction(Math.round(sign * p0), q0); // 返回上一项
            }

            p1 = p0; q1 = q0;
            p0 = p2; q0 = q2;
        }

        return new Fraction(Math.round(sign * p0), q0); // 最后一项
    }
    //连分数正向递推构建符合条件的分数（我不会）
    private static void buildContinuedFraction(double a, ArrayList<Integer> list, double errorLimit, int depth, int maxDepth) {
        if (depth >= maxDepth || Math.abs(a - Math.floor(a)) < errorLimit) return;

        int intPart = (int) Math.floor(a);
        list.add(intPart);

        double remainder = a - intPart;
        if (remainder < errorLimit) return;

        buildContinuedFraction(1.0 / remainder, list, errorLimit, depth + 1, maxDepth);
    }
    public static void main(String[] args){
        Fraction f1=new Fraction(Math.PI);
        System.out.println("f1="+f1);
        long startTime = System.nanoTime();//开始時間
        Fraction f2=roundFractionAbsErrorIterate(Math.PI,0,10000000000000l);
        System.out.println("f2="+f2);
        long consumingTime = System.nanoTime() - startTime;//消耗時間
        System.out.println("操作消耗" + consumingTime/1000 + "微秒");/*_000_000*/
    }
    public void roundFraction(Fraction f,double errorLimit,long maxDenominator){//todo

    }
    public Fraction(Fraction a){
        p=a.p;
        q=a.q;
    }
    private void constructFromDouble(double m){
        if(m==0.0){//是0的话直接默认值1/0
            return;
        }
        int log2Result=getExpPartValue(m);//指数值
        if(log2Result==1024){//NaN或者Infinity
            throw new IllegalArgumentException("Input used to construct a fraction from double should be a real number. The input="+m);
        }
        p=removeAllTrailing0sBin(getSignifPartLongValue(m));//提出数值部分并消去末尾0
        int n=countDigitsBin(p);//整数部分的数字数=整数部分的指数+1(因为把小数变成了整数相当于乘了许多2,这个n就等于乘的2的数量,到时候用于还原这个数用的)
        int shift=log2Result-n+1;//shift=double指数-(整数部分乘上的指数=(整数部分的数字数-1))=小数点应该向前/后移动的位数,m*2^n中的n
        if(shift>0){//如果shift>0说明p应该向左移(*2)
            if(shift>63-n){//确保数不会太大以至于分子溢出
                throw new IllegalArgumentException("Input used to construct a fraction from double should be less than . The input="+m);
            }
            p<<=shift;//p*2^shift
        }else{//如果shift<0说明q应该向左移(*2)(整体/2)
            if(shift<-62){//确保数不会太小以至于分母溢出
                p>>=(-shift-62);//让p先除一下2牺牲精度防止溢出
                if(p==0){//分母变成0的话分母默认为1就好
                    return;
                }
                shift=-62;//然后这样q就只需要位移62次即可
            }
            q<<=-shift;//q*2^-shift
        }
        p=(sign(m)==-1?-p:p);
    }
    private void constructFromFloat(float m){
        if(m==0.0f){
            return;
        }
        int log2Result=getExpPartValue(m);
        if(log2Result==128){// 255-127，表示NaN或Infinity
            throw new IllegalArgumentException("Input used to construct a fraction from float should be a real number. The input="+m);
        }
        p=removeAllTrailing0sBin(getSignifPartIntValue(m));
        int n=countDigitsBin(p);
        int shift=log2Result-n+1;
        if(shift>0){
            if(shift>63-n){
                throw new IllegalArgumentException("Input used to construct a fraction from float should be less than . The input="+m);
            }
            p<<=shift;
        }else{
            if(shift<-62){
                p>>=(-shift-62);
                if(p==0){
                    return;
                }
                shift=-62;
            }
            q<<=-shift;
        }
        p=(sign(m)==-1?-p:p);
    }

    private void constructFromDecimal(long m,long n){//小数m.n
        if(n==0){
            p=m;
        }else{
            n=removeAllTrailing0sDec(n);//去掉小数部分多余的0
            q=POW10LONG[countDigitsDec(n)];//小数有几位,q就有几位(m.xx=m*100+xx/100)
            p=m*q+n;
            this.sim();
        }
    }
    private void constructFromFraction(long m,long n){
        p=m;//p是分子
        q=n;//q是分母
        this.sim();
    }
    public Fraction(String input){//根据字符串来决定分数
        String[] parts;
        input=input.replace(",",".").replace("\\","/").trim();//统一格式
        if(input.matches("-?\\d*[./]-?\\d*")){//匹配数字+符号(./)+数字的结构(数字可无，会被替换为默认值)
            if(input.contains(".")){//如果有".",说明是小数
                parts=input.split("\\.",-1);//那么将字符串用"."分割为两半,-1用来防止一边没有字符
                constructFromDecimal(parts[0].isEmpty()?0:Long.parseLong(parts[0]),parts[1].isEmpty()?0:Long.parseLong(parts[1]));
            }else{//如果没有".",说明有"/",说明是分数,同时也可以处理整数
                parts=input.split("/",-1);
                constructFromFraction(parts[0].isEmpty()?0:Long.parseLong(parts[0]),parts[1].isEmpty()?1:Long.parseLong(parts[1]));
            }
        }else{
            throw new IllegalArgumentException("Invalid input format: not number+'.'or'/'+number format!");
        }
    }



    public String toString(){
        return (p+"/"+q);
    }
    public boolean equals(Fraction b){
        return p==b.p&&q==b.q;
    }
    public int hashCode(){
        return 0;
    }
    //sign中true表示+;false表示-;

    private void sim() {//约分reduction of a fraction
        if(q==0) {p=p/q;}//是零直接报错
        else if(p==0) {q=1;return;}//化简:分子为0,分母变1且不约分
        boolean sign=p>0==q>0;//记录整体符号
        p=p>0?p:-p;//取p的绝对值
        q=q>0?q:-q;//这三行-化简:上下负抵消|或|分母负变分子负
        long gcd=GCDStein(p,q);
        p/=gcd;q/=gcd;//先求最大公约数，再同时除以最大公约数，完成约分
        p=sign?p:-p;//再把符号还回分子
    }
    public Fraction simplify() {//约分reduction of a fraction
        Fraction a=new Fraction(p,q);//定义
        if(a.q==0){a.p=a.p/a.q;}//是零直接报错
        if(a.p==0){a.q=1;return new Fraction(0,1);}//化简:分子为0,分母变1且不约分
        boolean sign=a.p>0==a.q>0;//是零直接报错,同时记录整体符号，用除防溢出
        a.p=a.p>0?a.p:-a.p;//取p的绝对值
        a.q=a.q>0?a.q:-a.q;//这三行-化简:上下负抵消|或|分母负变分子负
        long gcd=GCDStein(a.p,a.q);
        p/=gcd;q/=gcd;//先求最大公约数，再同时除以最大公约数，完成约分
        a.p=sign?a.p:-a.p;//再把符号还回分子
        return a;
    }
    public Fraction add(Fraction input){//加add
        Fraction a=new Fraction(p,q);
        Fraction b=new Fraction(input);
        Fraction c=new Fraction(0);//定义
        //long ap=a.p,aq=q,bp=input.p,bq=input.q;
        a.sim();
        b.sim();//化简
        boolean a_sign=a.p>0==a.q>0;//记录整体符号
        a.p=a.p>0?a.p:-a.p;//取p的绝对值
        a.q=a.q>0?a.q:-a.q;//取q的绝对值//这三行-化简:上下负抵消|或|分母负变分子负
        boolean b_sign=b.p>0==b.q>0;//记录整体符号
        b.p=b.p>0?b.p:-b.p;
        b.q=b.q>0?b.q:-b.q;//同上
        long x=GCDStein(a.q,b.q);//求最大公因数
        a.q/=x;b.q/=x;//两个分母同除最大公因数
        c.q=x*a.q*b.q;//求分母,分数上下同时约掉一个最大公因数,还剩一个最大公约数
        c.p=(a_sign?a.p*b.q:-a.p*b.q)+(b_sign?b.p*a.q:-b.p*a.q);//求分子,?:式决定要不要正负号
        return c;
    }
    public Fraction addNotSim(Fraction b){//加add,但是不化简
        long q_=q*b.q;//分母:合成.相当于c.q
        return new Fraction(p*b.q+b.p*q,q_);//分子:通分,+分母并返回
    }
    public Fraction subtractNotSim(Fraction b){//减minus
        long q_=q*b.q;//分母:合成.相当于c.q
        return new Fraction(p*b.q-b.p*q,q_);//分子:通分,+分母并返回
    }
    public Fraction subtract(Fraction input){//减minus
        Fraction a=new Fraction(p,q);
        Fraction b=new Fraction(input);
        Fraction c=new Fraction(0);//定义
        a.sim();
        b.sim();//化简
        boolean a_sign=a.p>0==a.q>0;//记录整体符号
        a.p=a.p>0?a.p:-a.p;//取p的绝对值
        a.q=a.q>0?a.q:-a.q;//取q的绝对值//这三行-化简:上下负抵消|或|分母负变分子负
        boolean b_sign=b.p>0==b.q>0;//记录整体符号
        b.p=b.p>0?b.p:-b.p;
        b.q=b.q>0?b.q:-b.q;//同上
        long x=GCDStein(a.q,b.q);//求最大公因数
        a.q/=x;b.q/=x;//两个分母同除最大公因数
        c.q=x*a.q*b.q;//求分母,分数上下同时约掉一个最大公因数,还剩一个最大公约数
        c.p=(a_sign?a.p*b.q:-a.p*b.q)-(b_sign?b.p*a.q:-b.p*a.q);//求分子,?:式决定要不要正负号
        return c;
    }
    public Fraction multiply(Fraction input){//乘multiply
        Fraction a=new Fraction(p,q);
        Fraction b=new Fraction(input);//定义
        a.sim();
        b.sim();//化简
        boolean sign=(a.p>0==a.q>0)==(b.p>0==b.q>0);//记录整体符号
        a.p=a.p>0?a.p:-a.p;//取p的绝对值
        a.q=a.q>0?a.q:-a.q;//取q的绝对值//这三行-化简:上下负抵消|或|分母负变分子负
        b.p=b.p>0?b.p:-b.p;
        b.q=b.q>0?b.q:-b.q;//同上
        long x=GCDStein(a.p,b.q);//求最大公因数
        a.p/=x;b.q/=x;//a分子与b分母同除最大公因数,相当于约分
        x=GCDStein(b.p,a.q);//求最大公因数
        b.p/=x;a.q/=x;//b分子与a分母同除最大公因数,相当于约分
        return new Fraction(a.p*b.p*(sign?1:-1),a.q*b.q);//分子相乘,分母相乘
    }
    public Fraction divide(Fraction input){//除divide
        Fraction a=new Fraction(p,q);
        Fraction b=new Fraction(input);//定义
        a.sim();
        b.sim();//化简
        boolean sign=(a.p>0==a.q>0)==(b.p>0==b.q>0);//记录整体符号
        a.p=a.p>0?a.p:-a.p;//取p的绝对值
        a.q=a.q>0?a.q:-a.q;//取q的绝对值//这三行-化简:上下负抵消|或|分母负变分子负
        b.p=b.p>0?b.p:-b.p;
        b.q=b.q>0?b.q:-b.q;//同上
        long x=GCDStein(a.p,b.p);//求最大公因数
        a.p/=x;b.p/=x;//两个分子同除最大公因数,相当于约分
        x=GCDStein(a.q,b.q);//求最大公因数
        a.q/=x;b.q/=x;//两个分母同除最大公因数,相当于约分
        return new Fraction(a.p*b.q*(sign?1:-1),a.q*b.p);//a分子乘b分母,a分母乘b分子=c
    }
    public int compareTo(Fraction input){
        long ap=this.p;
        long bp=input.p;
        long aq=sign(ap);
        long bq=sign(bp);
        if(aq!=bq){
            return aq>bq?1:-1;
        }else if(aq==0){
            return 0;
        }
        aq=this.q;
        bq=input.q;
        long x=GCDStein(ap,bp);//求最大公因数
        ap/=x;bp/=x;//两个分子同除最大公因数,相当于约分
        x=GCDStein(aq,bq);//求最大公因数
        aq/=x;bq/=x;//两个分母同除最大公因数,相当于约分
        return intSign(ap*bq-bp*aq);
    }

    @Override
    public Fraction negative() {
        return new Fraction(-p,q);
    }
}
/*过期代码:
        generally:
        min=p>=q?Math.min(q,p-q):Math.min(p,q-p);//x取(p,q,|p-q|)的最小值(肯定是拿小的约啊)=Math.min
        for(long i=2;i<=min;i++){//化简:约分--要是x比i还小就没有必要再约下去了吧
            if(p%i==0&&q%i==0) {//判断整除
                p=p/i;q=q/i;min=min/i;//能除就除
                i--;//除完可能还能再除
            }
        }
        这是在求最大公约数
        add:
        long min=a.q<=b.q?a.q:b.q;//找a.q和b.q的最小值
        long x=1;//定义最大公因数
        for(long i=2;i<=min;i++){//找公因数
            if(a.q%i==0&&b.q%i==0){//能整除说明是公因数
                a.q=a.q/i;b.q=b.q/i;x=x*i;//分母同除该公因数,x乘以该公因数
                min=min/i;i--;//缩小范围,使i<根号(最初的min),同时i有可能还是他们的公因数
            }
        }
        multiply:
        for(long i=2;i<=a.p*b.q;i++){//化简:约分a.p和b.q
            if(a.p%i==0&&b.q%i==0){
                a.p=a.p/i;b.q=b.q/i;
                i--;
            }
        }
        for(long i=2;i<=b.p*a.q;i++){//化简:约分b.p和a.q//相当于约分了
            if(b.p%i==0&&a.q%i==0){
                b.p=b.p/i;a.q=a.q/i;
                i--;
            }
        }
        divide:
        for(long i=2;i<=a.p*b.p;i++){//化简:约分a.p和b.p
            if(a.p%i==0&&b.p%i==0){
                a.p=a.p/i;b.p=b.p/i;
                i--;
            }
        }
        for(long i=2;i<=b.q*a.q;i++){//化简:约分b.q和a.q//相当于约分了
            if(b.q%i==0&&a.q%i==0){
                b.q=b.q/i;a.q=a.q/i;
                i--;
            }
        }
        Constructor decimal:
        boolean bool=(m%1.0!=0.0);//判断是不是小数,如果这个数求余1为0说明这是个小数
        if(bool){//如果是小数
            while(m%1.0!=0.0){//那么重复执行直到刚好变成整数(刚好不是小数)
                q=q*2;//那么就把得数除以10(把分母乘以10),
                m=m*10.0;//原数乘以10
            }
        }
    2025.3.29: 让class变为immutable，仿照integer
*/