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
Rectangle 2 3;;
