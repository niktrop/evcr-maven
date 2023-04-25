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

let larger = \r1.\r2.  {
	if (r1.area r1 > r2.area r2) r1
	else r2
};
let r1 = Rectangle 2 2;
let r2 = PositionedRectangle 1 2 10 20;
let bigRect = larger r1 r2;
bigRect.toString bigRect;;
