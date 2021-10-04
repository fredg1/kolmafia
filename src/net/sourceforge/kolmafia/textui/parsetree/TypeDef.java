package net.sourceforge.kolmafia.textui.parsetree;

import org.eclipse.lsp4j.Location;

import net.sourceforge.kolmafia.textui.DataTypes;

public class TypeDef
	extends Type
{
	Type base;

	public TypeDef( final String name, final Type base, final Location location )
	{
		super( name, DataTypes.TYPE_TYPEDEF, location );
		this.base = base;
	}

	@Override
	public Type getBaseType()
	{
		return this.base.getBaseType();
	}

	@Override
	public Value initialValue()
	{
		return this.base.initialValue();
	}

	@Override
	public Value parseValue( final String name, final boolean returnDefault )
	{
		return this.base.parseValue( name, returnDefault );
	}

	@Override
	public Value initialValueExpression()
	{
		return new TypeInitializer( this.base.getBaseType() );
	}

	@Override
	public boolean equals( Type o )
	{
		if ( o instanceof TypeReference )
		{
			o = ((TypeReference) o).getTarget();
		}

		return o instanceof TypeDef && this.name.equals( o.name );
	}

	@Override
	public boolean isBad()
	{
		return this.base.isBad();
	}
}
