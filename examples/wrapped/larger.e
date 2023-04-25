let Rectangle_raw = \w.\h. {
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

let Rectangle_wrap = \r.
{
	width = r.width,
	height = r.height,
	area = r.area r,
	toString = r.toString r,
	()
};

let Rectangle = \w.\h.Rectangle_wrap (Rectangle_raw w h);

let PositionedRectangle_raw = \x.\y.\w.\h.
	let super = Rectangle_raw w h;
{
	x = x,
	y = y,

	toString = \this. {
		super.toString this ++ "(" ++ str(this.x) ++ "," ++ str(this.y) ++ ")"
	},

	super
};

let PositionedRectangle_wrap = \r.
	let super = Rectangle_wrap r;
{
	x = r.x,
	y = r.y,
	toString = r.toString r,
	super
};

let PositionedRectangle = \x.\y.\w.\h.
	PositionedRectangle_wrap (PositionedRectangle_raw x y w h);

let larger = \r1.\r2.
{
	if (r1.area > r2.area) r1 else r2
};
let r1 = Rectangle 2 2;
let r2 = PositionedRectangle 1 2 10 20;
let bigRect = larger r1 r2;
bigRect.toString;;

