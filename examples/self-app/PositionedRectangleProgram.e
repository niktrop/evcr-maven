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
let r = PositionedRectangle 1 2 3 4 in
"area=" ++ str(r.area r) ++ ", toString=" ++ r.toString r;;
