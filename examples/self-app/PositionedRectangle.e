let Rectangle = \w.\h. {
	width = w,
	height = h,

	area = \this. {
		this.width * this.height
	},

	toString = \this. {
		str(this.width) ++ "x" ++ str(this.height)
	},

	{}
};

let PositionedRectangle = \x.\y.\w.\h.
	let super = Rectangle w h;
{
	x = x,
	y = y,

	toString = \this.  {
		super.toString this ++ "(" ++ str(this.x) ++ "," ++ str(this.y) ++ ")"
	},

	super
};
PositionedRectangle;;
