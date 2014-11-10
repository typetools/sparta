import sparta.checkers.quals.Source;
import sparta.checkers.quals.FlowPermission;

import sparta.checkers.quals.Sink;
import static sparta.checkers.quals.FlowPermissionString.*;

public class EnumTest{
	
	public @Source({}) @Sink(ANY) enum EnumAnnoBottom{
		CONST;
	}
	public @Source({}) @Sink({}) enum EnumAnnoEmpty{
		CONST;
	}
	public enum EnumNoAnno{
		CONST;
	}

	void writeToSMS(@Sink(WRITE_SMS) Object o){	}
	void foo(){
		writeToSMS(EnumAnnoBottom.CONST);
		//:: error: (argument.type.incompatible)
		writeToSMS(EnumAnnoEmpty.CONST);
		writeToSMS(EnumNoAnno.CONST);
	}
}